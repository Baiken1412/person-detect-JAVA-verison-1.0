package com.ruoyi.project.caseapp.track.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.awt.Color;
import com.alibaba.fastjson.JSON;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.domain.AppTrack;
import com.ruoyi.project.caseapp.track.domain.EventTrackRelation;
import com.ruoyi.project.caseapp.track.mapper.CompositeEventMapper;
import com.ruoyi.project.caseapp.track.mapper.AppTrackMapper;
import com.ruoyi.project.caseapp.track.mapper.EventTrackRelationMapper;
import com.ruoyi.project.caseapp.track.service.ICompositeEventService;

/**
 * 复合事件Service业务层处理
 * 核心逻辑：应用层实时写入，基于30秒空闲检测算法
 *
 * @author ruoyi
 * @date 2025-12-23
 */
@Service
public class CompositeEventServiceImpl implements ICompositeEventService
{
    private static final Logger logger = LoggerFactory.getLogger(CompositeEventServiceImpl.class);

    @Autowired
    private CompositeEventMapper compositeEventMapper;

    @Autowired
    private AppTrackMapper appTrackMapper;

    @Autowired
    private EventTrackRelationMapper relationMapper;

    // 空闲时间阈值：30秒（毫秒）
    private static final long IDLE_THRESHOLD = 30 * 1000;

    /**
     * 查询复合事件
     *
     * @param id 复合事件主键
     * @return 复合事件
     */
    @Override
    public CompositeEvent selectCompositeEventById(Long id)
    {
        return compositeEventMapper.selectCompositeEventById(id);
    }

    /**
     * 查询复合事件列表
     *
     * @param compositeEvent 复合事件
     * @return 复合事件
     */
    @Override
    public List<CompositeEvent> selectCompositeEventList(CompositeEvent compositeEvent)
    {
        return compositeEventMapper.selectCompositeEventList(compositeEvent);
    }

    /**
     * 新增复合事件
     *
     * @param compositeEvent 复合事件
     * @return 结果
     */
    @Override
    public int insertCompositeEvent(CompositeEvent compositeEvent)
    {
        return compositeEventMapper.insertCompositeEvent(compositeEvent);
    }

    /**
     * 修改复合事件
     *
     * @param compositeEvent 复合事件
     * @return 结果
     */
    @Override
    public int updateCompositeEvent(CompositeEvent compositeEvent)
    {
        return compositeEventMapper.updateCompositeEvent(compositeEvent);
    }

    /**
     * 批量删除复合事件
     *
     * @param ids 需要删除的复合事件主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCompositeEventByIds(String ids)
    {
        String[] idArray = StringUtils.split(ids, ",");
        int deletedCount = 0;

        // 逐个删除事件（确保关系表记录也被删除）
        for (String idStr : idArray)
        {
            try
            {
                Long id = Long.parseLong(idStr.trim());
                deletedCount += deleteCompositeEventById(id);
            }
            catch (NumberFormatException e)
            {
                // 忽略无效的ID
            }
        }

        return deletedCount;
    }

    /**
     * 删除复合事件信息
     *
     * @param id 复合事件主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCompositeEventById(Long id)
    {
        // 先删除关系表中的记录
        relationMapper.deleteByEventId(id);

        // 再删除复合事件
        return compositeEventMapper.deleteCompositeEventById(id);
    }

    /**
     * 根据轨迹更新或创建复合事件（核心方法）
     * 应用层实时写入：当轨迹数据变化时，自动计算并更新复合事件
     *
     * 逻辑：
     * 1. 查询该轨迹前后30秒内的所有轨迹
     * 2. 使用30秒算法重新计算复合事件
     * 3. 更新或创建数据库中的复合事件记录
     *
     * @param track 新增或修改的轨迹
     */
    @Override
    @Transactional
    public void updateOrCreateCompositeEventByTrack(AppTrack track)
    {
        if (track == null || track.getPssj() == null)
        {
            return;
        }

        // 1. 查询该轨迹所在的时间窗口内的所有轨迹
        //    时间窗口：前后各扩展1小时，确保能覆盖到相关的复合事件
        Date trackTime = track.getPssj();
        Calendar cal = Calendar.getInstance();
        cal.setTime(trackTime);
        cal.add(Calendar.HOUR, -1);
        Date beginTime = cal.getTime();

        cal.setTime(trackTime);
        cal.add(Calendar.HOUR, 1);
        Date endTime = cal.getTime();

        AppTrack queryParam = new AppTrack();
        queryParam.getParams().put("beginPssj", beginTime);
        queryParam.getParams().put("endPssj", endTime);

        List<AppTrack> windowTracks = appTrackMapper.selectAllAppTrackForComposite(queryParam);

        if (windowTracks == null || windowTracks.isEmpty())
        {
            return;
        }

        // 2. 使用30秒算法计算复合事件
        List<CompositeEvent> newEvents = calculate30SecondCompositeEvents(windowTracks);

        // 3. 删除该时间窗口内的旧复合事件（通过关系表查询）
        for (AppTrack t : windowTracks)
        {
            // 通过关系表查询该轨迹所属的事件ID列表
            List<Long> eventIds = relationMapper.selectEventIdsByTrackId(t.getId());
            for (Long eventId : eventIds)
            {
                // 删除复合事件（service方法会自动删除关系表记录）
                deleteCompositeEventById(eventId);
            }
        }

        // 4. 插入新计算的复合事件，并保存到关系表
        for (CompositeEvent event : newEvents)
        {
            // 插入复合事件，获取自增ID
            compositeEventMapper.insertCompositeEvent(event);
            Long compositeEventId = event.getId();

            // 保存到关系表：建立事件与轨迹的关联
            if (compositeEventId != null && event.getTrackIds() != null && !event.getTrackIds().isEmpty())
            {
                String[] trackIdArray = event.getTrackIds().split(",");
                List<EventTrackRelation> relations = new ArrayList<>();

                for (int i = 0; i < trackIdArray.length; i++)
                {
                    try
                    {
                        Long trackId = Long.parseLong(trackIdArray[i].trim());
                        relations.add(new EventTrackRelation(compositeEventId, trackId, i + 1));
                    }
                    catch (NumberFormatException e)
                    {
                        // 忽略无效的ID
                    }
                }

                // 批量插入关系
                if (!relations.isEmpty())
                {
                    relationMapper.batchInsertRelations(relations);
                    System.out.println("  已保存 " + relations.size() + " 条轨迹关联到关系表");
                }
            }
        }
    }

    /**
     * 重新计算并同步所有复合事件
     * 用于初始化或数据修复
     *
     * @param appTrack 查询条件（时间范围等）
     * @return 同步的事件数量
     */
    @Override
    @Transactional
    public int syncAllCompositeEvents(AppTrack appTrack)
    {
        // 1. 查询所有轨迹数据
        List<AppTrack> allTracks = appTrackMapper.selectAllAppTrackForComposite(appTrack);

        if (allTracks == null || allTracks.isEmpty())
        {
            return 0;
        }

        // 2. 使用30秒算法计算复合事件
        List<CompositeEvent> events = calculate30SecondCompositeEvents(allTracks);

        // 3. 清空旧数据（如果有时间范围限制，只删除该范围内的）
        if (appTrack != null && appTrack.getParams().get("beginPssj") != null)
        {
            CompositeEvent queryParam = new CompositeEvent();
            queryParam.getParams().put("beginTime", appTrack.getParams().get("beginPssj"));
            queryParam.getParams().put("endTime", appTrack.getParams().get("endPssj"));
            List<CompositeEvent> oldEvents = compositeEventMapper.selectCompositeEventList(queryParam);
            for (CompositeEvent oldEvent : oldEvents)
            {
                // 删除复合事件（service方法会自动删除关系表记录）
                deleteCompositeEventById(oldEvent.getId());
            }
        }

        // 4. 批量插入新事件，并保存到关系表
        for (CompositeEvent event : events)
        {
            // 插入复合事件，获取自增ID
            compositeEventMapper.insertCompositeEvent(event);
            Long compositeEventId = event.getId();

            // 保存到关系表：建立事件与轨迹的关联
            if (compositeEventId != null && event.getTrackIds() != null && !event.getTrackIds().isEmpty())
            {
                String[] trackIdArray = event.getTrackIds().split(",");
                List<EventTrackRelation> relations = new ArrayList<>();

                for (int i = 0; i < trackIdArray.length; i++)
                {
                    try
                    {
                        Long trackId = Long.parseLong(trackIdArray[i].trim());
                        relations.add(new EventTrackRelation(compositeEventId, trackId, i + 1));
                    }
                    catch (NumberFormatException e)
                    {
                        // 忽略无效的ID
                    }
                }

                // 批量插入关系
                if (!relations.isEmpty())
                {
                    relationMapper.batchInsertRelations(relations);
                }
            }
        }

        System.out.println("同步完成，共生成 " + events.size() + " 个复合事件，已回填轨迹关联");
        return events.size();
    }

    /**
     * 使用30秒空闲检测算法计算复合事件
     *
     * 核心逻辑：
     * - 只要30秒内任意摄像头还检测到人，事件继续
     * - 只有所有摄像头都30秒没检测到人，事件才结束
     * - 相邻轨迹间隔可以超过30秒（人在不同区域移动）
     *
     * 算法：向前查看
     * - 对于每条轨迹，检查它之后30秒内是否还有轨迹
     * - 如果有 → 继续当前事件
     * - 如果没有 → 当前事件在此轨迹结束，下一条轨迹开始新事件
     *
     * @param tracks 轨迹列表（已按时间升序排序）
     * @return 复合事件列表
     */
    private List<CompositeEvent> calculate30SecondCompositeEvents(List<AppTrack> tracks)
    {
        if (tracks == null || tracks.isEmpty())
        {
            return new ArrayList<>();
        }

        // 按时间升序排序
        tracks.sort(Comparator.comparing(AppTrack::getPssj));

        List<CompositeEvent> compositeEvents = new ArrayList<>();
        List<AppTrack> currentEventTracks = new ArrayList<>();

        System.out.println("========== 复合事件计算开始（新算法：向前查看） ==========");
        System.out.println("总轨迹数：" + tracks.size());
        System.out.println("空闲阈值：" + (IDLE_THRESHOLD / 1000) + "秒");
        System.out.println("逻辑：只要30秒内任意摄像头还检测到人，事件继续");

        for (int i = 0; i < tracks.size(); i++)
        {
            AppTrack current = tracks.get(i);

            if (currentEventTracks.isEmpty())
            {
                // 开始新事件
                currentEventTracks.add(current);
                System.out.println("\n>>> 新事件开始 #" + (compositeEvents.size() + 1));
                System.out.println("  轨迹ID: " + current.getId() + ", 时间: " + current.getPssj() + ", 区域: " + current.getQymc());
            }
            else
            {
                // 将当前轨迹加入事件
                currentEventTracks.add(current);

                // 计算与上一条轨迹的间隔（仅用于日志）
                AppTrack lastTrack = currentEventTracks.get(currentEventTracks.size() - 2);
                long timeDiff = current.getPssj().getTime() - lastTrack.getPssj().getTime();
                long timeDiffSeconds = timeDiff / 1000;

                System.out.println("  + 轨迹ID: " + current.getId() + ", 时间: " + current.getPssj() + ", 区域: " + current.getQymc() + " (距上一条" + timeDiffSeconds + "秒)");
            }

            // 向前查看：检查当前轨迹之后30秒内是否还有轨迹
            boolean hasNextTrackWithin30Sec = false;
            if (i < tracks.size() - 1)
            {
                AppTrack nextTrack = tracks.get(i + 1);
                long timeToNext = nextTrack.getPssj().getTime() - current.getPssj().getTime();
                long timeToNextSeconds = timeToNext / 1000;

                if (timeToNext <= IDLE_THRESHOLD)
                {
                    hasNextTrackWithin30Sec = true;
                    System.out.println("    → 下一条轨迹在" + timeToNextSeconds + "秒后，事件继续");
                }
                else
                {
                    System.out.println("    → 下一条轨迹在" + timeToNextSeconds + "秒后(>" + (IDLE_THRESHOLD/1000) + "秒)，30秒空闲，事件结束");
                }
            }
            else
            {
                System.out.println("    → 已是最后一条轨迹，事件结束");
            }

            // 如果之后30秒内没有轨迹，结束当前事件
            if (!hasNextTrackWithin30Sec)
            {
                System.out.println("  当前事件包含 " + currentEventTracks.size() + " 条轨迹");
                CompositeEvent event = createCompositeEventFromTracks(currentEventTracks);
                compositeEvents.add(event);

                // 清空，准备下一个事件
                currentEventTracks = new ArrayList<>();
            }
        }

        System.out.println("\n========== 复合事件计算完成 ==========");
        System.out.println("共生成 " + compositeEvents.size() + " 个复合事件");
        for (int i = 0; i < compositeEvents.size(); i++)
        {
            CompositeEvent ce = compositeEvents.get(i);
            System.out.println("事件#" + (i+1) + ": " + ce.getStartTime() + " ~ " + ce.getEndTime() + ", 包含" + ce.getTrackCount() + "条轨迹");
        }
        System.out.println("=========================================\n");

        return compositeEvents;
    }

    /**
     * 从轨迹列表创建复合事件对象
     *
     * @param tracks 轨迹列表
     * @return 复合事件对象
     */
    private CompositeEvent createCompositeEventFromTracks(List<AppTrack> tracks)
    {
        if (tracks == null || tracks.isEmpty())
        {
            return null;
        }

        CompositeEvent event = new CompositeEvent();

        // 基本信息
        AppTrack firstTrack = tracks.get(0);
        AppTrack lastTrack = tracks.get(tracks.size() - 1);

        event.setEventId(firstTrack.getId());
        event.setStartTime(firstTrack.getPssj());
        event.setEndTime(lastTrack.getJssj()); // 使用结束时间（开始时间+5秒）
        event.setTrackCount(tracks.size());
        event.setIsClosed(1); // 默认已结束

        // 计算持续时长（秒）- 从第一条开始到最后一条结束
        long durationMillis = lastTrack.getJssj().getTime() - firstTrack.getPssj().getTime();
        event.setDuration((int) (durationMillis / 1000));

        // 聚合管理员信息（去重）
        Set<String> ryxmSet = new HashSet<>();
        Set<String> wlrySet = new HashSet<>();
        for (AppTrack track : tracks)
        {
            if (StringUtils.isNotEmpty(track.getRyxm()))
            {
                String[] names = track.getRyxm().split(",");
                for (String name : names)
                {
                    if (StringUtils.isNotEmpty(name.trim()))
                    {
                        ryxmSet.add(name.trim());
                    }
                }
            }
            if (StringUtils.isNotEmpty(track.getWlry()))
            {
                String[] names = track.getWlry().split(",");
                for (String name : names)
                {
                    if (StringUtils.isNotEmpty(name.trim()))
                    {
                        wlrySet.add(name.trim());
                    }
                }
            }
        }
        event.setRyxm(String.join(",", ryxmSet));
        event.setWlry(String.join(",", wlrySet));

        // 聚合区域信息
        Set<String> areasSet = new LinkedHashSet<>(); // 保持顺序
        Long mainQyid = null;
        String mainQymc = null;
        for (AppTrack track : tracks)
        {
            if (StringUtils.isNotEmpty(track.getQymc()))
            {
                areasSet.add(track.getQymc());
                if (mainQyid == null)
                {
                    mainQyid = track.getQyid();
                    mainQymc = track.getQymc();
                }
            }
        }
        event.setQyid(mainQyid);
        event.setQymc(mainQymc);
        event.setPathAreas(String.join(",", areasSet));

        // 统计最大人员数量
        int maxRysl = tracks.stream()
                .mapToInt(AppTrack::getRysl)
                .max()
                .orElse(0);
        event.setRyslMax(maxRysl);

        // 检查特殊情况
        int hasNonworktime = 0;
        int hasAbnormal = 0;

        for (AppTrack track : tracks)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(track.getPssj());
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            // 非工作时间：18:00-08:00
            if (hour < 8 || hour >= 18)
            {
                hasNonworktime = 1;
            }

            // 人员异常：双人操作要求，人数<2（单人）或≥3（超员）
            // 注意：rysl为int类型，默认值为0，0表示无人员数据
            if (track.getRysl() > 0)
            {
                if (track.getRysl() < 2 || track.getRysl() >= 3)
                {
                    hasAbnormal = 1;
                }
            }
        }
        event.setHasNonworktime(hasNonworktime);
        event.setHasAbnormalPerson(hasAbnormal);

        // 判断标注状态
        boolean allLabeled = tracks.stream()
                .allMatch(track -> "1".equals(track.getBzzt()));
        event.setBzzt(allLabeled ? "1" : "0");

        // 聚合行为原因
        Set<String> xwyySet = tracks.stream()
                .filter(track -> StringUtils.isNotEmpty(track.getXwyy()))
                .map(AppTrack::getXwyy)
                .collect(Collectors.toSet());
        event.setXwyy(String.join(";", xwyySet));

        // 保存轨迹ID列表
        String trackIds = tracks.stream()
                .map(track -> String.valueOf(track.getId()))
                .collect(Collectors.joining(","));
        event.setTrackIds(trackIds);

        // 生成行为描述
        String behaviorDescription = generateBehaviorDescription(event, tracks);
        event.setBehaviorDescription(behaviorDescription);

        // 设置初始处理状态
        event.setProcessStatus(allLabeled ? "已完成" : "待处理");

        return event;
    }

    /**
     * 自动生成行为描述
     *
     * @param event 复合事件
     * @param tracks 轨迹列表
     * @return 行为描述文本
     */
    private String generateBehaviorDescription(CompositeEvent event, List<AppTrack> tracks)
    {
        StringBuilder desc = new StringBuilder();

        // 1. 基本信息：时间段、区域
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        desc.append(String.format("人员于%s在%s出现",
            sdf.format(event.getStartTime()),
            event.getQymc() != null ? event.getQymc() : "监控区域"
        ));

        // 2. 路径信息
        if (event.getPathAreas() != null && event.getPathAreas().contains(","))
        {
            desc.append("，经过路径：").append(event.getPathAreas().replace(",", " → "));
        }

        // 3. 人数信息
        if (event.getRyslMax() > 0)
        {
            desc.append("，最多").append(event.getRyslMax()).append("人");
        }

        // 4. 持续时长
        int duration = event.getDuration();
        if (duration >= 60)
        {
            int minutes = duration / 60;
            int seconds = duration % 60;
            desc.append("，持续").append(minutes).append("分");
            if (seconds > 0)
            {
                desc.append(seconds).append("秒");
            }
        }
        else
        {
            desc.append("，持续").append(duration).append("秒");
        }

        // 5. 特殊标签
        List<String> tags = new ArrayList<>();
        if (event.getHasNonworktime() != null && event.getHasNonworktime() == 1)
        {
            tags.add("非工作时间");
        }
        if (event.getHasAbnormalPerson() != null && event.getHasAbnormalPerson() == 1)
        {
            tags.add("人数异常");
        }
        if (StringUtils.isNotEmpty(event.getWlry()))
        {
            tags.add("外来人员(" + event.getWlry() + ")");
        }
        if (event.getDuration() > 1800)
        {
            tags.add("停留时间较长");
        }

        if (!tags.isEmpty())
        {
            desc.append("。【").append(String.join("、", tags)).append("】");
        }
        else
        {
            desc.append("。");
        }

        // 6. 轨迹数量
        if (event.getTrackCount() > 1)
        {
            desc.append("期间共检测到").append(event.getTrackCount()).append("次活动");
        }

        return desc.toString();
    }

    /**
     * 标注复合事件
     * 标注时会同时更新：
     * 1. 复合事件表的标注信息
     * 2. 该事件下所有轨迹的标注信息
     *
     * @param eventId 复合事件的event_id（第一条轨迹的ID）
     * @param xwyy 行为原因
     * @param ryxm 人员姓名
     * @param wlry 外来人员
     * @param remark 备注
     */
    @Override
    @Transactional
    public void annotateCompositeEvent(Long eventId, String xwyy, String ryxm, String wlry, String remark)
    {
        // 1. 查询复合事件
        CompositeEvent queryParam = new CompositeEvent();
        queryParam.setEventId(eventId);
        List<CompositeEvent> events = compositeEventMapper.selectCompositeEventList(queryParam);

        if (events == null || events.isEmpty())
        {
            throw new RuntimeException("未找到事件ID为 " + eventId + " 的复合事件");
        }

        CompositeEvent compositeEvent = events.get(0);

        // 2. 更新复合事件的标注信息
        compositeEvent.setBzzt("1"); // 已标注
        compositeEvent.setProcessStatus("已完成"); // 标注后设为已完成
        compositeEvent.setXwyy(xwyy);
        compositeEvent.setRyxm(ryxm);
        compositeEvent.setWlry(wlry);
        compositeEvent.setRemark(remark);
        compositeEventMapper.updateCompositeEvent(compositeEvent);

        System.out.println("已标注复合事件 #" + eventId + ": " + xwyy);

        // 3. 从关系表查询该复合事件下的所有轨迹ID
        List<Long> trackIds = relationMapper.selectTrackIdsByEventId(compositeEvent.getId());

        if (trackIds != null && !trackIds.isEmpty())
        {
            int updatedCount = 0;

            // 4. 更新所有轨迹的标注信息
            for (Long trackId : trackIds)
            {
                try
                {
                    AppTrack trackToUpdate = new AppTrack();
                    trackToUpdate.setId(trackId);
                    trackToUpdate.setBzzt("1"); // 已标注
                    trackToUpdate.setXwyy(xwyy);
                    trackToUpdate.setRyxm(ryxm);
                    trackToUpdate.setWlry(wlry);
                    trackToUpdate.setRemark(remark);
                    trackToUpdate.setBzsj(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

                    appTrackMapper.updateAppTrack(trackToUpdate);
                    updatedCount++;
                }
                catch (Exception e)
                {
                    System.err.println("更新轨迹 " + trackId + " 失败: " + e.getMessage());
                }
            }

            System.out.println("已同步更新 " + updatedCount + " 条轨迹的标注信息");
        }
        else
        {
            System.out.println("警告：复合事件 #" + eventId + " 没有关联的轨迹");
        }
    }

    /**
     * 根据eventId查询复合事件
     *
     * @param eventId 事件ID（第一条轨迹的ID）
     * @return 复合事件
     */
    @Override
    public CompositeEvent selectCompositeEventByEventId(Long eventId)
    {
        CompositeEvent query = new CompositeEvent();
        query.setEventId(eventId);
        List<CompositeEvent> list = compositeEventMapper.selectCompositeEventList(query);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /**
     * 处理轨迹删除后的复合事件清理
     * 应用层实时写入：删除轨迹后自动删除包含该轨迹的所有复合事件
     *
     * 逻辑：
     * 1. 通过关系表查询包含该轨迹的所有复合事件
     * 2. 删除这些复合事件（同时会删除关系表记录）
     * 3. 可选：重新计算周围轨迹的复合事件（暂不实现，避免过度复杂）
     *
     * @param track 被删除的轨迹
     */
    @Override
    @Transactional
    public void handleTrackDeletion(AppTrack track)
    {
        if (track == null || track.getId() == null)
        {
            return;
        }

        try
        {
            // 通过关系表查询该轨迹所属的事件ID列表
            List<Long> eventIds = relationMapper.selectEventIdsByTrackId(track.getId());

            if (eventIds != null && !eventIds.isEmpty())
            {
                System.out.println("轨迹 ID=" + track.getId() + " 被删除，需要删除 " + eventIds.size() + " 个包含该轨迹的复合事件");

                // 删除所有包含该轨迹的复合事件
                for (Long eventId : eventIds)
                {
                    // deleteCompositeEventById 方法已经包含删除关系表记录的逻辑
                    deleteCompositeEventById(eventId);
                    System.out.println("  已删除复合事件 ID=" + eventId);
                }

                System.out.println("轨迹删除后复合事件清理完成");
            }
            else
            {
                System.out.println("轨迹 ID=" + track.getId() + " 不属于任何复合事件，无需清理");
            }
        }
        catch (Exception e)
        {
            System.err.println("处理轨迹删除时发生错误：" + e.getMessage());
            e.printStackTrace();
            throw e;  // 重新抛出异常，确保事务回滚
        }
    }

    @Value("${ruoyi.profile}")
    private String uploadPath;

    /**
     * 导出事件包（HTML报告 + 图片 + 视频 + JSON数据）
     * 生成包含事件完整信息的ZIP压缩包，供浏览器下载
     *
     * @param eventId 复合事件ID
     * @param exportPath 导出根路径（下载目录）
     * @return ZIP文件名（用于浏览器下载）
     */
    @Override
    public String exportEventPackage(Long eventId, String exportPath) throws Exception
    {
        try
        {
            // 1. 查询复合事件数据（兼容主键ID和eventId）
            CompositeEvent event = selectCompositeEventByEventId(eventId);
            if (event == null)
            {
                // 如果通过eventId查不到，尝试通过主键ID查询
                event = selectCompositeEventById(eventId);
                if (event == null)
                {
                    throw new Exception("复合事件不存在：ID=" + eventId);
                }
            }

            // 重要：关系表中的event_id关联的是app_composite_event.id（主键），而不是event_id字段
            // 所以应该使用复合事件的主键ID来查询关联轨迹
            Long compositeEventId = event.getId();

            // 2. 查询关联的轨迹列表
            List<Long> trackIds = relationMapper.selectTrackIdsByEventId(compositeEventId);
            if (trackIds == null || trackIds.isEmpty())
            {
                throw new Exception("复合事件无关联轨迹：compositeEventId=" + compositeEventId);
            }

            List<AppTrack> tracks = new ArrayList<>();
            for (Long trackId : trackIds)
            {
                AppTrack track = appTrackMapper.selectAppTrackById(trackId);
                if (track != null)
                {
                    tracks.add(track);
                }
            }

            // 3. 创建临时导出目录
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            String folderName = String.format("事件包_COMP%d_%s", event.getEventId(), timestamp);

            // 使用系统临时目录
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Path packagePath = tempDir.resolve(folderName);
            Files.createDirectories(packagePath);
            Files.createDirectories(packagePath.resolve("images"));
            Files.createDirectories(packagePath.resolve("videos"));
            Files.createDirectories(packagePath.resolve("data"));

            // 4. 复制图片和视频文件
            copyMediaFiles(tracks, packagePath);

            // 5. 生成JSON数据文件
            generateJsonData(event, tracks, packagePath);

            // 6. 生成HTML报告
            generateHtmlReport(event, tracks, packagePath);

            // 7. 生成README文件
            generateReadme(event, packagePath);

            // 8. 生成PDF说明文档
            generatePdfReport(event, tracks, packagePath);

            // 9. 将文件夹打包成ZIP文件
            String zipFileName = folderName + ".zip";
            Path zipFilePath = Paths.get(exportPath, zipFileName);
            zipFolder(packagePath, zipFilePath);

            // 10. 删除临时文件夹
            deleteDirectory(packagePath.toFile());

            logger.info("事件包导出成功：{}", zipFilePath);

            // 返回ZIP文件名（仅文件名，不含路径）
            return zipFileName;
        }
        catch (Exception e)
        {
            logger.error("导出事件包失败：eventId=" + eventId, e);
            throw e;
        }
    }

    /**
     * 复制媒体文件（图片和视频）
     */
    private void copyMediaFiles(List<AppTrack> tracks, Path packagePath) throws IOException
    {
        int imageIndex = 1;
        int videoIndex = 1;

        for (AppTrack track : tracks)
        {
            // 复制图片
            if (StringUtils.isNotEmpty(track.getPstp()))
            {
                String imagePath = track.getPstp();
                // 移除URL前缀，获取实际文件路径
                if (imagePath.startsWith("http"))
                {
                    int profileIndex = imagePath.indexOf("/profile/");
                    if (profileIndex != -1)
                    {
                        imagePath = imagePath.substring(profileIndex);
                    }
                }
                File sourceImage = new File(uploadPath + imagePath.replace("/profile", ""));
                if (sourceImage.exists())
                {
                    int dotIndex = imagePath.lastIndexOf(".");
                    String ext = (dotIndex != -1) ? imagePath.substring(dotIndex) : ".jpg";
                    String newName = String.format("track_%03d%s", imageIndex++, ext);
                    Path targetImage = packagePath.resolve("images").resolve(newName);
                    Files.copy(sourceImage.toPath(), targetImage, StandardCopyOption.REPLACE_EXISTING);
                    track.setPstp("images/" + newName);  // 更新为相对路径
                }
            }

            // 复制视频
            if (StringUtils.isNotEmpty(track.getSpdz()))
            {
                String videoPath = track.getSpdz();
                if (videoPath.startsWith("http"))
                {
                    int profileIndex = videoPath.indexOf("/profile/");
                    if (profileIndex != -1)
                    {
                        videoPath = videoPath.substring(profileIndex);
                    }
                }
                File sourceVideo = new File(uploadPath + videoPath.replace("/profile", ""));
                if (sourceVideo.exists())
                {
                    int dotIndex = videoPath.lastIndexOf(".");
                    String ext = (dotIndex != -1) ? videoPath.substring(dotIndex) : ".mp4";
                    String newName = String.format("track_%03d%s", videoIndex++, ext);
                    Path targetVideo = packagePath.resolve("videos").resolve(newName);
                    Files.copy(sourceVideo.toPath(), targetVideo, StandardCopyOption.REPLACE_EXISTING);
                    track.setSpdz("videos/" + newName);  // 更新为相对路径
                }
            }
        }
    }

    /**
     * 生成JSON数据文件
     */
    private void generateJsonData(CompositeEvent event, List<AppTrack> tracks, Path packagePath) throws IOException
    {
        Map<String, Object> data = new HashMap<>();
        data.put("event", event);
        data.put("tracks", tracks);
        data.put("exportTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        String json = JSON.toJSONString(data, true);
        Path jsonFile = packagePath.resolve("data").resolve("event_data.json");
        Files.write(jsonFile, json.getBytes("UTF-8"));
    }

    /**
     * 生成README文件
     */
    private void generateReadme(CompositeEvent event, Path packagePath) throws IOException
    {
        StringBuilder readme = new StringBuilder();
        readme.append("===============================================\n");
        readme.append("           资产视频事件包\n");
        readme.append("===============================================\n\n");
        readme.append("事件ID: COMP").append(event.getEventId()).append("\n");
        readme.append("开始时间: ").append(event.getStartTime()).append("\n");
        readme.append("结束时间: ").append(event.getEndTime()).append("\n");
        readme.append("标注状态: ").append("1".equals(event.getBzzt()) ? "已标注" : "待标注").append("\n");
        if (StringUtils.isNotEmpty(event.getXwyy()))
        {
            readme.append("行为原因: ").append(event.getXwyy()).append("\n");
        }
        readme.append("导出时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        readme.append("===============================================\n");
        readme.append("文件说明:\n");
        readme.append("  index.html     - 事件报告主页面（双击打开）\n");
        readme.append("  data/          - 结构化数据文件\n");
        readme.append("  images/        - 关键帧截图\n");
        readme.append("  videos/        - 事件视频片段\n");
        readme.append("===============================================\n");

        Path readmeFile = packagePath.resolve("README.txt");
        Files.write(readmeFile, readme.toString().getBytes("UTF-8"));
    }

    /**
     * 生成HTML报告（将在下一步实现）
     */
    private void generateHtmlReport(CompositeEvent event, List<AppTrack> tracks, Path packagePath) throws IOException
    {
        // HTML模板将在后续创建
        String html = buildHtmlTemplate(event, tracks);
        Path htmlFile = packagePath.resolve("index.html");
        Files.write(htmlFile, html.getBytes("UTF-8"));
    }

    /**
     * 构建HTML模板内容
     */
    private String buildHtmlTemplate(CompositeEvent event, List<AppTrack> tracks)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='zh-CN'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>事件经过还原 - COMP").append(event.getEventId()).append("</title>\n");
        html.append("    <style>\n");
        html.append(getHtmlStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class='container'>\n");
        html.append("        <div class='header'>\n");
        html.append("            <h1>🎬 事件经过还原（复合事件）</h1>\n");
        html.append("        </div>\n");
        html.append("        <div class='content'>\n");

        // 复合事件信息
        String statusText = "1".equals(event.getBzzt()) ? "已标注" : "待标注";
        String statusColor = "1".equals(event.getBzzt()) ? "#51cf66" : "#ff6b6b";

        html.append("            <div class='composite-info'>\n");
        html.append("                <div class='composite-info-title'>\n");
        html.append("                    复合事件 #COMP").append(event.getEventId()).append(" - ").append(tracks.size()).append(" 个关联事件\n");
        html.append("                    <span style='background: ").append(statusColor).append("; color: white; padding: 2px 8px; border-radius: 10px; font-size: 11px; margin-left: 8px;'>").append(statusText).append("</span>\n");
        html.append("                </div>\n");
        // 格式化时间显示
        String startTimeStr = event.getStartTime() != null ? sdf.format(event.getStartTime()) : "";
        String endTimeStr = "";
        if (event.getEndTime() != null) {
            String fullEndTime = sdf.format(event.getEndTime());
            String[] parts = fullEndTime.split(" ");
            endTimeStr = parts.length > 1 ? parts[1] : fullEndTime;
        }

        html.append("                <div class='composite-info-detail'>\n");
        html.append("                    📅 时间范围：").append(startTimeStr).append(" ~ ").append(endTimeStr).append("<br>\n");
        html.append("                    📍 路径：").append(event.getPathAreas() != null ? event.getPathAreas() : "无路径信息").append("<br>\n");
        html.append("                    ⏱️ 导出时间：").append(sdf.format(new Date())).append("\n");
        html.append("                </div>\n");

        // 标注信息
        if ("1".equals(event.getBzzt()))
        {
            html.append("                <div style='margin-top: 12px; padding: 12px; background: #e7f5ff; border-left: 4px solid #4c6ef5; border-radius: 4px;'>\n");
            html.append("                    <div style='font-weight: bold; color: #4c6ef5; margin-bottom: 6px;'>📋 标注信息</div>\n");
            if (StringUtils.isNotEmpty(event.getXwyy()))
            {
                html.append("                    <div style='margin-bottom: 4px;'>📝 行为原因：").append(event.getXwyy()).append("</div>\n");
            }
            if (StringUtils.isNotEmpty(event.getRyxm()))
            {
                html.append("                    <div style='margin-bottom: 4px;'>👤 管理员：").append(event.getRyxm()).append("</div>\n");
            }
            if (StringUtils.isNotEmpty(event.getWlry()))
            {
                html.append("                    <div style='margin-bottom: 4px;'>🔶 外来人员：").append(event.getWlry()).append("</div>\n");
            }
            if (StringUtils.isNotEmpty(event.getRemark()))
            {
                html.append("                    <div style='margin-bottom: 4px;'>💬 备注：").append(event.getRemark()).append("</div>\n");
            }
            html.append("                </div>\n");
        }
        html.append("            </div>\n");

        // 视频播放器
        html.append("            <div class='video-container'>\n");
        html.append("                <video id='traceVideo' controls>\n");
        html.append("                    <source src='' type='video/mp4'>\n");
        html.append("                    您的浏览器不支持视频播放。\n");
        html.append("                </video>\n");
        html.append("            </div>\n");

        // 截图轮播
        html.append("            <div class='image-carousel'>\n");
        html.append("                <div class='carousel-header'>\n");
        html.append("                    <h3>📸 轨迹截图</h3>\n");
        html.append("                    <span class='carousel-counter' id='imageCounter'>1 / ").append(tracks.size()).append("</span>\n");
        html.append("                </div>\n");
        html.append("                <div class='carousel-container'>\n");
        html.append("                    <button class='carousel-btn prev' id='prevImageBtn' onclick='showPrevImage()'>◀</button>\n");
        html.append("                    <div class='carousel-image-wrapper'>\n");
        html.append("                        <img id='carouselImage' src='' alt='轨迹截图'>\n");
        html.append("                        <div id='noImagePlaceholder' class='no-image-placeholder' style='display: none;'>\n");
        html.append("                            <div>📷</div>\n");
        html.append("                            <p>暂无截图</p>\n");
        html.append("                        </div>\n");
        html.append("                        <div class='carousel-image-info' id='imageInfo'>\n");
        html.append("                            <div class='info-time'></div>\n");
        html.append("                            <div class='info-area'></div>\n");
        html.append("                        </div>\n");
        html.append("                    </div>\n");
        html.append("                    <button class='carousel-btn next' id='nextImageBtn' onclick='showNextImage()'>▶</button>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");

        // 时间线
        html.append("            <div class='trace-timeline' id='traceTimeline'>\n");
        for (int i = 0; i < tracks.size(); i++)
        {
            AppTrack track = tracks.get(i);
            // 格式化时间显示
            String trackTime = track.getPssj() != null ? sdf.format(track.getPssj()) : "";

            html.append("                <div class='trace-node' id='traceNode").append(i).append("' onclick='playTraceVideo(").append(i).append(")'>\n");
            html.append("                    <div class='trace-time'>").append(trackTime).append("</div>\n");
            html.append("                    <div class='trace-area'>📍 ").append(track.getQymc());
            if (track.getRysl() > 0)
            {
                html.append(" (").append(track.getRysl()).append("人)");
            }
            html.append("</div>\n");
            html.append("                </div>\n");
        }
        html.append("            </div>\n");

        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    <div class='footer'>\n");
        html.append("        <p>资产视频分析系统 - 事件包导出 © ").append(new SimpleDateFormat("yyyy").format(new Date())).append("</p>\n");
        html.append("    </div>\n");

        // JavaScript
        html.append("    <script>\n");
        html.append(getHtmlScripts(tracks));
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * HTML样式
     */
    private String getHtmlStyles()
    {
        return "* { margin: 0; padding: 0; box-sizing: border-box; }\n" +
               "body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f7fa; color: #333; padding: 20px; }\n" +
               ".container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); overflow: hidden; }\n" +
               ".header { background: linear-gradient(135deg, #9775fa 0%, #764ba2 100%); color: white; padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; }\n" +
               ".header h1 { font-size: 24px; font-weight: 600; }\n" +
               ".content { padding: 20px 30px; }\n" +
               ".composite-info { padding: 15px; background: #f8f9fa; border-radius: 6px; margin-bottom: 20px; }\n" +
               ".composite-info-title { font-weight: bold; color: #9775fa; margin-bottom: 8px; font-size: 16px; }\n" +
               ".composite-info-detail { color: #666; font-size: 14px; line-height: 1.6; }\n" +
               ".video-container { width: 100%; background: #000; border-radius: 6px; overflow: hidden; margin-bottom: 20px; }\n" +
               ".video-container video { width: 100%; height: auto; display: block; }\n" +
               ".image-carousel { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }\n" +
               ".carousel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 2px solid #9775fa; }\n" +
               ".carousel-header h3 { font-size: 18px; color: #333; margin: 0; }\n" +
               ".carousel-counter { font-size: 14px; color: #666; background: #f0f0f0; padding: 4px 12px; border-radius: 12px; }\n" +
               ".carousel-container { display: flex; align-items: center; gap: 15px; position: relative; }\n" +
               ".carousel-btn { flex-shrink: 0; width: 50px; height: 50px; background: #9775fa; color: white; border: none; border-radius: 50%; font-size: 20px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(151,117,250,0.3); }\n" +
               ".carousel-btn:hover:not(:disabled) { background: #764ba2; transform: scale(1.1); box-shadow: 0 4px 12px rgba(151,117,250,0.5); }\n" +
               ".carousel-btn:disabled { background: #ccc; cursor: not-allowed; opacity: 0.5; }\n" +
               ".carousel-image-wrapper { flex: 1; position: relative; background: #f8f9fa; border-radius: 8px; overflow: hidden; min-height: 400px; display: flex; align-items: center; justify-content: center; }\n" +
               ".carousel-image-wrapper img { width: 100%; height: auto; max-height: 500px; object-fit: contain; display: block; }\n" +
               ".carousel-image-info { position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(to top, rgba(0,0,0,0.8), transparent); color: white; padding: 20px 15px 10px; font-size: 14px; }\n" +
               ".carousel-image-info .info-time { font-weight: bold; margin-bottom: 4px; font-size: 15px; }\n" +
               ".carousel-image-info .info-area { opacity: 0.9; }\n" +
               ".no-image-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; color: #999; font-size: 16px; padding: 60px; }\n" +
               ".no-image-placeholder div:first-child { font-size: 64px; margin-bottom: 10px; opacity: 0.5; }\n" +
               ".trace-timeline { border-left: 3px solid #9775fa; padding-left: 20px; margin-top: 20px; }\n" +
               ".trace-node { margin-bottom: 15px; padding: 15px; border-radius: 6px; position: relative; background: #f8f9fa; cursor: pointer; transition: all 0.3s; }\n" +
               ".trace-node:hover { background: #e7f5ff; }\n" +
               ".trace-node::before { content: ''; position: absolute; left: -26px; top: 20px; width: 16px; height: 16px; background: #bbb; border-radius: 50%; border: 3px solid #fff; transition: all 0.3s; }\n" +
               ".trace-node.active { background: #e7f5ff; border-left: 3px solid #9775fa; box-shadow: 0 2px 8px rgba(151,117,250,0.2); }\n" +
               ".trace-node.active::before { background: #9775fa; transform: scale(1.2); }\n" +
               ".trace-time { font-weight: bold; color: #9775fa; font-size: 16px; margin-bottom: 5px; }\n" +
               ".trace-area { color: #666; font-size: 14px; margin-top: 5px; }\n" +
               ".trace-duration { font-size: 12px; color: #999; margin-top: 5px; }\n" +
               ".footer { background: #2c3e50; color: #ecf0f1; text-align: center; padding: 20px; margin-top: 40px; }";
    }

    /**
     * HTML脚本
     */
    private String getHtmlScripts(List<AppTrack> tracks)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder js = new StringBuilder();
        js.append("const tracks = [\n");
        for (int i = 0; i < tracks.size(); i++)
        {
            AppTrack track = tracks.get(i);
            // 格式化时间
            String trackTime = track.getPssj() != null ? sdf.format(track.getPssj()) : "";

            js.append("    { image: '").append(track.getPstp() != null ? track.getPstp() : "").append("', ");
            js.append("video: '").append(track.getSpdz() != null ? track.getSpdz() : "").append("', ");
            js.append("time: '").append(trackTime).append("', ");
            js.append("area: '").append(track.getQymc()).append("', ");
            js.append("rysl: ").append(track.getRysl()).append(" }");
            if (i < tracks.size() - 1) js.append(",");
            js.append("\n");
        }
        js.append("];\n\n");
        js.append("let currentImageIndex = 0;\n\n");

        // 播放视频函数
        js.append("function playTraceVideo(index) {\n");
        js.append("    if (index >= tracks.length) return;\n");
        js.append("    const v = document.getElementById('traceVideo');\n");
        js.append("    const track = tracks[index];\n");
        js.append("    highlightTraceNode(index);\n");
        js.append("    updateCarouselImage(index);\n");
        js.append("    if (track.video && track.video.trim() !== '') {\n");
        js.append("        v.src = track.video;\n");
        js.append("        v.play();\n");
        js.append("    } else {\n");
        js.append("        v.removeAttribute('src');\n");
        js.append("    }\n");
        js.append("}\n\n");

        // 高亮节点函数
        js.append("function highlightTraceNode(index) {\n");
        js.append("    document.querySelectorAll('.trace-node').forEach(n => n.classList.remove('active'));\n");
        js.append("    const node = document.getElementById('traceNode' + index);\n");
        js.append("    if (node) {\n");
        js.append("        node.classList.add('active');\n");
        js.append("        node.scrollIntoView({ behavior: 'smooth', block: 'nearest' });\n");
        js.append("    }\n");
        js.append("}\n\n");

        // 更新截图轮播
        js.append("function updateCarouselImage(index) {\n");
        js.append("    if (index >= tracks.length) return;\n");
        js.append("    currentImageIndex = index;\n");
        js.append("    const track = tracks[index];\n");
        js.append("    const img = document.getElementById('carouselImage');\n");
        js.append("    const placeholder = document.getElementById('noImagePlaceholder');\n");
        js.append("    const counter = document.getElementById('imageCounter');\n");
        js.append("    const imageInfo = document.getElementById('imageInfo');\n");
        js.append("    const prevBtn = document.getElementById('prevImageBtn');\n");
        js.append("    const nextBtn = document.getElementById('nextImageBtn');\n");
        js.append("    counter.textContent = (index + 1) + ' / ' + tracks.length;\n");
        js.append("    prevBtn.disabled = (index === 0);\n");
        js.append("    nextBtn.disabled = (index === tracks.length - 1);\n");
        js.append("    imageInfo.querySelector('.info-time').textContent = '🕐 ' + track.time;\n");
        js.append("    imageInfo.querySelector('.info-area').textContent = '📍 ' + track.area + (track.rysl ? ' (' + track.rysl + '人)' : '');\n");
        js.append("    if (track.image && track.image.trim() !== '') {\n");
        js.append("        img.src = track.image;\n");
        js.append("        img.style.display = 'block';\n");
        js.append("        placeholder.style.display = 'none';\n");
        js.append("    } else {\n");
        js.append("        img.style.display = 'none';\n");
        js.append("        placeholder.style.display = 'flex';\n");
        js.append("    }\n");
        js.append("}\n\n");

        // 上一张截图
        js.append("function showPrevImage() {\n");
        js.append("    if (currentImageIndex > 0) {\n");
        js.append("        const newIndex = currentImageIndex - 1;\n");
        js.append("        updateCarouselImage(newIndex);\n");
        js.append("        highlightTraceNode(newIndex);\n");
        js.append("    }\n");
        js.append("}\n\n");

        // 下一张截图
        js.append("function showNextImage() {\n");
        js.append("    if (currentImageIndex < tracks.length - 1) {\n");
        js.append("        const newIndex = currentImageIndex + 1;\n");
        js.append("        updateCarouselImage(newIndex);\n");
        js.append("        highlightTraceNode(newIndex);\n");
        js.append("    }\n");
        js.append("}\n\n");

        // 页面加载时初始化
        js.append("window.addEventListener('DOMContentLoaded', function() {\n");
        js.append("    if (tracks.length > 0) {\n");
        js.append("        updateCarouselImage(0);\n");
        js.append("        highlightTraceNode(0);\n");
        js.append("    }\n");
        js.append("});\n");

        return js.toString();
    }

    /**
     * 生成PDF事件说明文档（方案3：表格版）
     */
    private void generatePdfReport(CompositeEvent event, List<AppTrack> tracks, Path packagePath) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");

        PDDocument document = new PDDocument();
        try
        {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 加载中文字体（使用系统字体）
            PDFont font = loadChineseFont(document);
            PDFont boldFont = font; // PDFBox 2.0 doesn't have bold variant easily, use same font

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 50;
            float yPosition = pageHeight - margin;
            float fontSize = 12;
            float titleFontSize = 16;
            float lineHeight = 20;

            // 标题
            contentStream.setFont(boldFont, titleFontSize);
            String title = "资产视频监控事件说明书";
            float titleWidth = boldFont.getStringWidth(title) / 1000 * titleFontSize;
            contentStream.beginText();
            contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, yPosition);
            contentStream.showText(title);
            contentStream.endText();

            yPosition -= 40;

            // 绘制表格边框
            contentStream.setLineWidth(1f);

            // 基本信息表格
            float tableWidth = pageWidth - 2 * margin;
            float tableTop = yPosition;
            float rowHeight = 25;

            // 表格数据
            String[][] basicInfo = {
                {"事件编号", "COMP-" + (event.getEventId() != null ? event.getEventId() : "N/A")},
                {"事件时间", (event.getStartTime() != null ? dateOnly.format(event.getStartTime()) : "") + " " +
                            (event.getStartTime() != null ? timeOnly.format(event.getStartTime()) : "") + "-" +
                            (event.getEndTime() != null ? timeOnly.format(event.getEndTime()) : "")},
                {"持续时长", (event.getDuration() != null ? event.getDuration() + "分钟" : "N/A")},
                {"主要区域", event.getQymc() != null ? event.getQymc() : ""},
                {"行为路径", event.getPathAreas() != null ? event.getPathAreas() : ""},
                {"标注状态", "1".equals(event.getBzzt()) ? "已标注" : "待标注"},
                {"行为原因", event.getXwyy() != null ? event.getXwyy() : ""},
                {"涉及人员", event.getRyxm() != null ? event.getRyxm() : ""},
                {"最大人数", event.getRyslMax() != null ? event.getRyslMax() + "人" : ""},
                {"轨迹数量", event.getTrackCount() != null ? event.getTrackCount() + "条" : tracks.size() + "条"}
            };

            contentStream.setFont(font, fontSize);

            // 绘制基本信息表格
            for (int i = 0; i < basicInfo.length; i++)
            {
                float y = tableTop - i * rowHeight;

                // 绘制行边框
                contentStream.addRect(margin, y - rowHeight, tableWidth, rowHeight);
                contentStream.stroke();

                // 绘制中间分隔线
                contentStream.moveTo(margin + 100, y);
                contentStream.lineTo(margin + 100, y - rowHeight);
                contentStream.stroke();

                // 写入标签（左列）
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 10, y - 17);
                contentStream.showText(basicInfo[i][0]);
                contentStream.endText();

                // 写入值（右列）
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 110, y - 17);
                String value = basicInfo[i][1];
                if (value.length() > 45)
                {
                    value = value.substring(0, 42) + "...";
                }
                contentStream.showText(value);
                contentStream.endText();
            }

            yPosition = tableTop - basicInfo.length * rowHeight - 30;

            // 轨迹明细标题
            contentStream.setFont(boldFont, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("轨迹明细");
            contentStream.endText();

            yPosition -= 30;

            // 轨迹表格表头
            String[] headers = {"序号", "时间", "区域", "人数"};
            float[] columnWidths = {50, 150, 200, 80};

            contentStream.setFont(font, fontSize);

            // 绘制表头
            float xPos = margin;
            contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
            contentStream.stroke();

            for (int i = 0; i < headers.length; i++)
            {
                if (i > 0)
                {
                    contentStream.moveTo(xPos, yPosition);
                    contentStream.lineTo(xPos, yPosition - rowHeight);
                    contentStream.stroke();
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(xPos + 10, yPosition - 17);
                contentStream.showText(headers[i]);
                contentStream.endText();

                xPos += columnWidths[i];
            }

            yPosition -= rowHeight;

            // 绘制轨迹数据行（最多显示10条）
            int maxRows = Math.min(tracks.size(), 10);

            // 如果没有轨迹数据，显示提示信息
            if (maxRows == 0)
            {
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.stroke();
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 10, yPosition - 17);
                contentStream.showText("无轨迹数据");
                contentStream.endText();
                yPosition -= rowHeight;
            }

            for (int i = 0; i < maxRows; i++)
            {
                AppTrack track = tracks.get(i);

                // 检查是否需要新页面
                if (yPosition < margin + 50)
                {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(font, fontSize);
                    yPosition = pageHeight - margin;
                }

                String ryslValue = "0";
                try {
                    ryslValue = String.valueOf(track.getRysl());
                } catch (Exception e) {
                    // 如果获取人数失败，使用默认值0
                }

                String[] rowData = {
                    String.valueOf(i + 1),
                    track.getPssj() != null ? timeOnly.format(track.getPssj()) : "",
                    track.getQymc() != null ? track.getQymc() : "",
                    ryslValue
                };

                xPos = margin;
                contentStream.addRect(margin, yPosition - rowHeight, tableWidth, rowHeight);
                contentStream.stroke();

                for (int j = 0; j < rowData.length; j++)
                {
                    if (j > 0)
                    {
                        contentStream.moveTo(xPos, yPosition);
                        contentStream.lineTo(xPos, yPosition - rowHeight);
                        contentStream.stroke();
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPos + 10, yPosition - 17);
                    contentStream.showText(rowData[j]);
                    contentStream.endText();

                    xPos += columnWidths[j];
                }

                yPosition -= rowHeight;
            }

            yPosition -= 30;

            // 异常提示
            if (yPosition < margin + 100)
            {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(font, fontSize);
                yPosition = pageHeight - margin;
            }

            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("异常提示:");
            contentStream.endText();

            yPosition -= 20;

            String nonWorktimeStatus = (event.getHasNonworktime() != null && event.getHasNonworktime() == 1) ? "■" : "□";
            String abnormalPersonStatus = (event.getHasAbnormalPerson() != null && event.getHasAbnormalPerson() == 1) ? "■" : "□";

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(nonWorktimeStatus + " 非工作时间进入    " + abnormalPersonStatus + " 人员数量异常");
            contentStream.endText();

            yPosition -= 40;

            // 导出信息
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("导出信息:");
            contentStream.endText();

            yPosition -= 20;

            String[] exportInfo = {
                "导出时间: " + sdf.format(new Date()),
                "文件位置: " + packagePath.getFileName().toString() + "/",
                "包含文件: index.html, images/, videos/, data/"
            };

            for (String info : exportInfo)
            {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(info);
                contentStream.endText();
                yPosition -= 20;
            }

            contentStream.close();

            // 保存PDF
            Path pdfPath = packagePath.resolve("事件说明.pdf");
            document.save(pdfPath.toFile());

            logger.info("PDF事件说明文档生成成功: {}", pdfPath);
        }
        finally
        {
            document.close();
        }
    }

    /**
     * 加载中文字体
     */
    private PDFont loadChineseFont(PDDocument document) throws IOException
    {
        // 尝试加载Windows系统中文字体
        String[] fontPaths = {
            "C:/Windows/Fonts/simhei.ttf",  // 黑体
            "C:/Windows/Fonts/simsun.ttc",  // 宋体
            "C:/Windows/Fonts/msyh.ttc",    // 微软雅黑
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf", // Linux
            "/System/Library/Fonts/PingFang.ttc"  // macOS
        };

        for (String fontPath : fontPaths)
        {
            File fontFile = new File(fontPath);
            if (fontFile.exists())
            {
                return PDType0Font.load(document, fontFile);
            }
        }

        // 如果都找不到，抛出异常
        throw new IOException("无法找到中文字体文件，请确保系统已安装中文字体");
    }

    /**
     * 统计今日复合事件总数
     */
    @Override
    public int countTodayEvents()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date beginTime = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endTime = cal.getTime();

        CompositeEvent queryParam = new CompositeEvent();
        queryParam.getParams().put("beginTime", beginTime);
        queryParam.getParams().put("endTime", endTime);

        List<CompositeEvent> events = compositeEventMapper.selectCompositeEventList(queryParam);
        return events != null ? events.size() : 0;
    }

    /**
     * 统计待标注复合事件数量
     */
    @Override
    public int countUnlabeledEvents()
    {
        CompositeEvent queryParam = new CompositeEvent();
        queryParam.setBzzt("0");

        List<CompositeEvent> events = compositeEventMapper.selectCompositeEventList(queryParam);
        return events != null ? events.size() : 0;
    }

    /**
     * 统计已标注复合事件数量
     */
    @Override
    public int countLabeledEvents()
    {
        CompositeEvent queryParam = new CompositeEvent();
        queryParam.setBzzt("1");

        List<CompositeEvent> events = compositeEventMapper.selectCompositeEventList(queryParam);
        return events != null ? events.size() : 0;
    }

    /**
     * 统计本月复合事件总数
     */
    @Override
    public int countMonthEvents()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date beginTime = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        Date endTime = cal.getTime();

        CompositeEvent queryParam = new CompositeEvent();
        queryParam.getParams().put("beginTime", beginTime);
        queryParam.getParams().put("endTime", endTime);

        List<CompositeEvent> events = compositeEventMapper.selectCompositeEventList(queryParam);
        return events != null ? events.size() : 0;
    }

    /**
     * 批量导出事件包
     * 将多个事件包导出到一个文件夹
     *
     * @param eventIds 事件ID列表
     * @param exportBasePath 导出基础路径
     * @return 导出文件夹路径
     */
    @Override
    public String batchExportEventPackages(List<Long> eventIds, String exportBasePath) throws Exception
    {
        // 边界检查：事件ID列表
        if (eventIds == null || eventIds.isEmpty()) {
            throw new IllegalArgumentException("事件ID列表不能为空");
        }

        // 边界检查：导出路径
        if (exportBasePath == null || exportBasePath.trim().isEmpty()) {
            throw new IllegalArgumentException("导出路径不能为空");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String batchFolderName = "batch_events_" + timestamp;

        // 使用系统临时目录
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path batchFolder = tempDir.resolve(batchFolderName);

        // 创建目录并处理可能的异常
        try {
            Files.createDirectories(batchFolder);
        } catch (Exception e) {
            throw new Exception("创建导出目录失败: " + e.getMessage(), e);
        }

        logger.info("开始批量导出事件包，共 {} 个事件，导出目录: {}", eventIds.size(), batchFolder);

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorLog = new StringBuilder();

        // 为每个事件导出单独的事件包（解压后的文件夹）
        for (Long eventId : eventIds)
        {
            try
            {
                logger.info("正在导出事件 {}/{}: eventId={}", successCount + failCount + 1, eventIds.size(), eventId);

                // 查询复合事件数据
                CompositeEvent event = selectCompositeEventByEventId(eventId);
                if (event == null) {
                    event = selectCompositeEventById(eventId);
                    if (event == null) {
                        throw new Exception("复合事件不存在：ID=" + eventId);
                    }
                }

                Long compositeEventId = event.getId();
                List<Long> trackIds = relationMapper.selectTrackIdsByEventId(compositeEventId);
                if (trackIds == null || trackIds.isEmpty()) {
                    throw new Exception("复合事件无关联轨迹：compositeEventId=" + compositeEventId);
                }

                List<AppTrack> tracks = new ArrayList<>();
                for (Long trackId : trackIds) {
                    AppTrack track = appTrackMapper.selectAppTrackById(trackId);
                    if (track != null) {
                        tracks.add(track);
                    }
                }

                // 创建事件包文件夹
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String eventTimestamp = sdf.format(new Date());
                String folderName = String.format("事件包_COMP%d_%s", event.getEventId(), eventTimestamp);
                Path packagePath = batchFolder.resolve(folderName);
                Files.createDirectories(packagePath);
                Files.createDirectories(packagePath.resolve("images"));
                Files.createDirectories(packagePath.resolve("videos"));
                Files.createDirectories(packagePath.resolve("data"));

                // 生成文件
                copyMediaFiles(tracks, packagePath);
                generateJsonData(event, tracks, packagePath);
                generateHtmlReport(event, tracks, packagePath);
                generateReadme(event, packagePath);
                generatePdfReport(event, tracks, packagePath);

                successCount++;
                logger.info("事件 {} 导出成功", eventId);
            }
            catch (Exception e)
            {
                failCount++;
                String errorMsg = "事件 " + eventId + " 导出失败: " + e.getMessage();
                logger.error(errorMsg, e);
                errorLog.append(errorMsg).append("\n");
            }
        }

        // 生成批量导出摘要
        generateBatchExportSummary(batchFolder, eventIds.size(), successCount, failCount, errorLog.toString());

        // 打包成ZIP文件
        String zipFileName = batchFolderName + ".zip";
        Path zipFilePath = Paths.get(exportBasePath, zipFileName);
        zipFolder(batchFolder, zipFilePath);

        // 删除临时文件夹
        deleteDirectory(batchFolder.toFile());

        logger.info("批量导出完成！成功: {}, 失败: {}, ZIP文件: {}", successCount, failCount, zipFilePath);

        // 返回ZIP文件名（仅文件名，不含路径）
        return zipFileName;
    }

    /**
     * 生成批量导出摘要文件
     */
    private void generateBatchExportSummary(Path batchFolder, int total, int success, int fail, String errorLog) throws IOException
    {
        StringBuilder summary = new StringBuilder();
        summary.append("===============================================\n");
        summary.append("           批量事件包导出摘要\n");
        summary.append("===============================================\n\n");
        summary.append("导出时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        summary.append("总事件数: ").append(total).append("\n");
        summary.append("成功导出: ").append(success).append("\n");
        summary.append("导出失败: ").append(fail).append("\n\n");

        if (fail > 0 && errorLog.length() > 0)
        {
            summary.append("===============================================\n");
            summary.append("失败详情:\n");
            summary.append("===============================================\n");
            summary.append(errorLog);
        }

        summary.append("\n===============================================\n");
        summary.append("使用说明:\n");
        summary.append("  每个事件包都包含独立的 index.html 文件\n");
        summary.append("  双击任意 index.html 即可查看该事件详情\n");
        summary.append("===============================================\n");

        Path summaryFile = batchFolder.resolve("导出摘要.txt");
        Files.write(summaryFile, summary.toString().getBytes("UTF-8"));
    }

    /**
     * 将文件夹打包成ZIP文件
     */
    private void zipFolder(Path sourceFolder, Path zipFilePath) throws IOException
    {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos))
        {
            Files.walk(sourceFolder)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try
                    {
                        Path relativePath = sourceFolder.relativize(path);
                        ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace("\\", "/"));
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    }
                    catch (IOException e)
                    {
                        logger.error("打包文件失败: " + path, e);
                    }
                });
        }
    }

    /**
     * 递归删除目录（可选使用）
     */
    private void deleteDirectory(File directory) throws IOException
    {
        if (directory.isDirectory())
        {
            File[] files = directory.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}
