package com.ruoyi.project.caseapp.track.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
        event.setEndTime(lastTrack.getPssj());
        event.setTrackCount(tracks.size());
        event.setIsClosed(1); // 默认已结束

        // 计算持续时长（秒）
        long durationMillis = lastTrack.getPssj().getTime() - firstTrack.getPssj().getTime();
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

            // 人员异常：>2人
            if (track.getRysl() > 2)
            {
                hasAbnormal = 1;
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

        return event;
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
        compositeEvent.setXwyy(xwyy);
        compositeEvent.setRyxm(ryxm);
        compositeEvent.setWlry(wlry);
        compositeEvent.setRemark(remark);
        compositeEventMapper.updateCompositeEvent(compositeEvent);

        System.out.println("已标注复合事件 #" + eventId + ": " + xwyy);

        // 3. 更新该事件下所有轨迹的标注信息
        if (compositeEvent.getTrackIds() != null && !compositeEvent.getTrackIds().isEmpty())
        {
            String[] trackIdArray = compositeEvent.getTrackIds().split(",");
            int updatedCount = 0;

            for (String trackIdStr : trackIdArray)
            {
                try
                {
                    Long trackId = Long.parseLong(trackIdStr.trim());
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
                catch (NumberFormatException e)
                {
                    // 忽略无效的ID
                }
            }

            System.out.println("已同步更新 " + updatedCount + " 条轨迹的标注信息");
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
}
