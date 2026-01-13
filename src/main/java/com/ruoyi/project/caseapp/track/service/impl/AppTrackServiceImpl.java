package com.ruoyi.project.caseapp.track.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.project.caseapp.track.mapper.AppTrackMapper;
import com.ruoyi.project.caseapp.track.mapper.EventTrackRelationMapper;
import com.ruoyi.project.caseapp.track.domain.AppTrack;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.service.IAppTrackService;
import com.ruoyi.project.caseapp.track.service.ICompositeEventService;
import com.ruoyi.common.utils.text.Convert;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;

/**
 * 轨迹Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-11
 */
@Service
public class AppTrackServiceImpl implements IAppTrackService
{
    private static final Logger logger = LoggerFactory.getLogger(AppTrackServiceImpl.class);
    @Autowired
    private AppTrackMapper appTrackMapper;

    @Autowired
    private ICompositeEventService compositeEventService;

    // 轨迹时长报警阈值（分钟），从配置文件读取
    @Value("${alarm.track-duration-threshold:3}")
    private int trackDurationThreshold;

    @Autowired
    private EventTrackRelationMapper relationMapper;

    /**
     * 初始化方法：打印配置信息
     */
    @PostConstruct
    public void init() {
        logger.warn("==========================================================");
        logger.warn("轨迹时长报警阈值配置：trackDurationThreshold = {} 分钟", trackDurationThreshold);
        logger.warn("==========================================================");
    }

    /**
     * 查询轨迹
     *
     * @param id 轨迹主键
     * @return 轨迹
     */
    @Override
    public AppTrack selectAppTrackById(Long id)
    {
        AppTrack track = appTrackMapper.selectAppTrackById(id);

        // 移除查询时的实时更新逻辑（性能优化）
        // 新数据会在 insertAppTrack 和 updateAppTrack 中自动计算
        // if (track != null) {
        //     calculateAndUpdateIfNeeded(track);
        // }

        return track;
    }

    /**
     * 查询轨迹列表
     *
     * @param appTrack 轨迹
     * @return 轨迹
     */
    @Override
    public List<AppTrack> selectAppTrackList(AppTrack appTrack)
    {
        List<AppTrack> tracks = appTrackMapper.selectAppTrackList(appTrack);

        // 移除查询时的实时更新逻辑（性能优化）
        // 新数据会在 insertAppTrack 和 updateAppTrack 中自动计算
        // if (tracks != null) {
        //     for (AppTrack track : tracks) {
        //         calculateAndUpdateIfNeeded(track);
        //     }
        // }

        return tracks;
    }

    /**
     * 新增轨迹
     * 应用层实时写入：插入轨迹后自动更新复合事件
     *
     * @param appTrack 轨迹
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAppTrack(AppTrack appTrack)
    {
        // 自动计算轨迹时长和是否过长
        calculateTrackDuration(appTrack);

        int result = appTrackMapper.insertAppTrack(appTrack);
        if (result > 0)
        {
            // 实时更新复合事件（方案B）
            compositeEventService.updateOrCreateCompositeEventByTrack(appTrack);
        }
        return result;
    }

    /**
     * 修改轨迹
     * 应用层实时写入：更新轨迹后自动重新计算复合事件
     *
     * @param appTrack 轨迹
     * @return 结果
     */
    @Override
    public int updateAppTrack(AppTrack appTrack)
    {
        // 自动计算轨迹时长和是否过长
        calculateTrackDuration(appTrack);

        int result = appTrackMapper.updateAppTrack(appTrack);
        if (result > 0)
        {
            // 实时更新复合事件（方案B）
            compositeEventService.updateOrCreateCompositeEventByTrack(appTrack);
        }
        return result;
    }

    /**
     * 计算轨迹时长和是否时间过长
     *
     * @param track 轨迹对象
     */
    private void calculateTrackDuration(AppTrack track)
    {
        if (track.getPssj() != null && track.getJssj() != null)
        {
            long durationMillis = track.getJssj().getTime() - track.getPssj().getTime();
            int durationSeconds = (int) (durationMillis / 1000);
            int durationMinutes = durationSeconds / 60;

            track.setTrackDuration(durationSeconds);
            track.setIsLongTrack(durationMinutes > trackDurationThreshold ? 1 : 0);

            logger.debug("轨迹 ID={} 时长计算完成：开始={}, 结束={}, 时长={}秒({}分钟), 阈值={}分钟, 是否过长={}",
                track.getId(), track.getPssj(), track.getJssj(), durationSeconds, durationMinutes,
                trackDurationThreshold, track.getIsLongTrack());
        }
        else
        {
            track.setTrackDuration(null);
            track.setIsLongTrack(0);

            logger.warn("轨迹 ID={} 无法计算时长：pssj={}, jssj={} （缺少开始或结束时间）",
                track.getId(), track.getPssj(), track.getJssj());
        }
    }

    /**
     * 如果需要则计算并更新轨迹时长
     * 用于处理 Python 等外部程序插入的数据
     * 注意：只更新时长字段，不触发复合事件重新计算，避免破坏业务逻辑
     *
     * @param track 轨迹对象
     */
    private void calculateAndUpdateIfNeeded(AppTrack track)
    {
        // 检查是否需要计算/重新计算时长
        if (track.getPssj() != null && track.getJssj() != null)
        {
            try {
                long durationMillis = track.getJssj().getTime() - track.getPssj().getTime();
                int durationSeconds = (int) (durationMillis / 1000);
                int durationMinutes = durationSeconds / 60;
                int expectedIsLongTrack = durationMinutes > trackDurationThreshold ? 1 : 0;

                // 判断是否需要更新：
                // 1) track_duration为NULL
                // 2) track_duration与实际时长不匹配
                // 3) is_long_track与当前阈值判断不匹配（阈值配置改变的情况）
                boolean needUpdate = false;
                String updateReason = "";

                if (track.getTrackDuration() == null) {
                    needUpdate = true;
                    updateReason = "track_duration为NULL";
                } else if (track.getTrackDuration() != durationSeconds) {
                    needUpdate = true;
                    updateReason = String.format("track_duration不匹配：数据库=%d秒，实际=%d秒",
                        track.getTrackDuration(), durationSeconds);
                } else if (track.getIsLongTrack() == null || track.getIsLongTrack() != expectedIsLongTrack) {
                    needUpdate = true;
                    updateReason = String.format("is_long_track不匹配：数据库=%s，期望=%d（阈值=%d分钟，时长=%d分钟）",
                        track.getIsLongTrack(), expectedIsLongTrack, trackDurationThreshold, durationMinutes);
                }

                if (needUpdate) {
                    track.setTrackDuration(durationSeconds);
                    track.setIsLongTrack(expectedIsLongTrack);

                    // 仅更新时长字段到数据库，不触发复合事件更新
                    AppTrack updateTrack = new AppTrack();
                    updateTrack.setId(track.getId());
                    updateTrack.setTrackDuration(durationSeconds);
                    updateTrack.setIsLongTrack(expectedIsLongTrack);
                    appTrackMapper.updateAppTrack(updateTrack);

                    logger.info("自动更新轨迹 ID={}, 原因：{}, 更新后：时长={}秒({}分钟), 是否过长={}",
                        track.getId(), updateReason, durationSeconds, durationMinutes, expectedIsLongTrack);
                }

            } catch (Exception e) {
                logger.error("自动计算轨迹 ID={} 时长失败", track.getId(), e);
            }
        }
    }

    /**
     * 批量删除轨迹
     * 应用层实时写入：删除轨迹后自动清理相关复合事件
     *
     * @param ids 需要删除的轨迹主键
     * @return 结果
     */
    @Override
    public int deleteAppTrackByIds(String ids)
    {
        String[] idArray = Convert.toStrArray(ids);
        int deletedCount = 0;

        // 逐个删除轨迹（确保复合事件也被正确处理）
        for (String idStr : idArray)
        {
            try
            {
                Long id = Long.parseLong(idStr.trim());
                deletedCount += deleteAppTrackById(id);
            }
            catch (NumberFormatException e)
            {
                // 忽略无效的ID
            }
        }

        return deletedCount;
    }

    /**
     * 删除轨迹信息
     * 应用层实时写入：删除轨迹后自动清理相关复合事件
     *
     * @param id 轨迹主键
     * @return 结果
     */
    @Override
    public int deleteAppTrackById(Long id)
    {
        // 先查询该轨迹，用于后续复合事件处理
        AppTrack track = appTrackMapper.selectAppTrackById(id);
        if (track == null)
        {
            return 0;
        }

        // 删除轨迹
        int result = appTrackMapper.deleteAppTrackById(id);

        if (result > 0)
        {
            // 删除包含该轨迹的所有复合事件
            // 因为轨迹已被删除，事件将失去完整性，需要删除并重新计算
            compositeEventService.handleTrackDeletion(track);
        }

        return result;
    }

    /**
     * 统计今日事件总数
     * 
     * @return 今日事件总数
     */
    @Override
    public int countTodayEvents()
    {
        return appTrackMapper.countTodayEvents();
    }

    /**
     * 统计待标注事件数（bzzt = "0"）
     * 
     * @return 待标注事件数
     */
    @Override
    public int countUnlabeledEvents()
    {
        return appTrackMapper.countUnlabeledEvents();
    }

    /**
     * 统计已标注事件数（bzzt = "1"）
     * 
     * @return 已标注事件数
     */
    @Override
    public int countLabeledEvents()
    {
        return appTrackMapper.countLabeledEvents();
    }

    /**
     * 统计本月事件总数
     * 
     * @return 本月事件总数
     */
    @Override
    public int countMonthEvents()
    {
        return appTrackMapper.countMonthEvents();
    }

    /**
     * 查询复合事件列表
     * 新方案（应用层实时写入）：直接从数据库查询复合事件，不再实时计算
     * - 数据已在轨迹插入/更新时由 compositeEventService 实时写入数据库
     * - 查询时直接读取，提升性能
     *
     * @param appTrack 轨迹（用于时间筛选）
     * @return 复合事件集合
     */
    @Override
    public List<CompositeEvent> selectCompositeEvents(AppTrack appTrack)
    {
        // 构建查询条件
        CompositeEvent queryParam = new CompositeEvent();

        // 传递时间范围筛选条件
        if (appTrack != null && appTrack.getParams() != null)
        {
            Object beginPssj = appTrack.getParams().get("beginPssj");
            Object endPssj = appTrack.getParams().get("endPssj");

            if (beginPssj != null && endPssj != null)
            {
                queryParam.getParams().put("beginTime", beginPssj);
                queryParam.getParams().put("endTime", endPssj);
            }
        }

        // 传递其他筛选条件（区域、状态、人员、原因等）
        if (appTrack != null)
        {
            if (appTrack.getQymc() != null)
            {
                queryParam.setQymc(appTrack.getQymc());
            }
            if (appTrack.getBzzt() != null)
            {
                queryParam.setBzzt(appTrack.getBzzt());
            }
            if (appTrack.getRyxm() != null)
            {
                queryParam.setRyxm(appTrack.getRyxm());
            }
            if (appTrack.getXwyy() != null && !appTrack.getXwyy().trim().isEmpty())
            {
                // 支持多个原因筛选（逗号分隔）
                String[] reasons = appTrack.getXwyy().split(",");
                List<String> reasonList = new ArrayList<>();
                for (String reason : reasons)
                {
                    if (reason != null && !reason.trim().isEmpty())
                    {
                        reasonList.add(reason.trim());
                    }
                }
                if (!reasonList.isEmpty())
                {
                    queryParam.getParams().put("xwyyList", reasonList);
                }
            }
            // 修复：传递轨迹时间过长筛选条件
            if (appTrack.getIsLongTrack() != null)
            {
                queryParam.setHasLongTrack(appTrack.getIsLongTrack());
            }
        }

        // 从数据库查询复合事件
        List<CompositeEvent> compositeEvents = compositeEventService.selectCompositeEventList(queryParam);

        // 为每个复合事件填充轨迹详情（用于前端展示）
        for (CompositeEvent event : compositeEvents)
        {
            List<AppTrack> tracks = new ArrayList<>();

            // 从关系表查询轨迹ID列表（已按seq_no排序）
            List<Long> trackIds = relationMapper.selectTrackIdsByEventId(event.getId());

            for (Long trackId : trackIds)
            {
                AppTrack track = appTrackMapper.selectAppTrackById(trackId);
                if (track != null)
                {
                    // 移除查询时的实时更新逻辑（性能优化）
                    // 新数据会在 insertAppTrack 和 updateAppTrack 中自动计算
                    // calculateAndUpdateIfNeeded(track);
                    tracks.add(track);
                }
            }

            // 确保 events 字段始终有值（即使是空数组），避免前端报错
            event.setEvents(tracks);

            // 根据实际轨迹数据重新计算复合事件的时间范围
            // 修复：当轨迹jssj被更新后，复合事件的end_time没有同步更新的问题
            if (!tracks.isEmpty())
            {
                Date minStartTime = null;
                Date maxEndTime = null;

                for (AppTrack track : tracks)
                {
                    // 找最早的开始时间
                    if (minStartTime == null || (track.getPssj() != null && track.getPssj().before(minStartTime)))
                    {
                        minStartTime = track.getPssj();
                    }

                    // 找最晚的结束时间
                    Date trackEndTime = track.getJssj();
                    if (trackEndTime == null && track.getPssj() != null)
                    {
                        // 如果没有结束时间，使用开始时间+5秒作为默认值
                        trackEndTime = new Date(track.getPssj().getTime() + 5000);
                    }
                    if (maxEndTime == null || (trackEndTime != null && trackEndTime.after(maxEndTime)))
                    {
                        maxEndTime = trackEndTime;
                    }
                }

                // 更新复合事件的时间范围
                if (minStartTime != null)
                {
                    event.setStartTime(minStartTime);
                }
                if (maxEndTime != null)
                {
                    event.setEndTime(maxEndTime);
                }

                // 重新计算持续时长
                if (minStartTime != null && maxEndTime != null)
                {
                    long durationMillis = maxEndTime.getTime() - minStartTime.getTime();
                    event.setDuration((int) (durationMillis / 1000));
                }
            }
        }

        return compositeEvents;
    }

    /**
     * 创建复合事件对象
     */
    private CompositeEvent createCompositeEvent(Long eventId, Date startTime, Date endTime, List<AppTrack> events)
    {
        CompositeEvent compositeEvent = new CompositeEvent();
        compositeEvent.setEventId(eventId);
        compositeEvent.setStartTime(startTime);
        compositeEvent.setEndTime(endTime);
        compositeEvent.setEvents(events);
        compositeEvent.setEventCount(events.size());

        // 判断标注状态：如果所有事件都已标注则为"1"，否则为"0"
        boolean allLabeled = true;
        for (AppTrack event : events)
        {
            if (event.getBzzt() == null || !"1".equals(event.getBzzt()))
            {
                allLabeled = false;
                break;
            }
        }
        compositeEvent.setBzzt(allLabeled ? "1" : "0");

        return compositeEvent;
    }
}
