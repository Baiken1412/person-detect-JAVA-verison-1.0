package com.ruoyi.project.caseapp.track.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.caseapp.track.mapper.AppTrackMapper;
import com.ruoyi.project.caseapp.track.mapper.EventTrackRelationMapper;
import com.ruoyi.project.caseapp.track.domain.AppTrack;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.service.IAppTrackService;
import com.ruoyi.project.caseapp.track.service.ICompositeEventService;
import com.ruoyi.common.utils.text.Convert;

/**
 * 轨迹Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
@Service
public class AppTrackServiceImpl implements IAppTrackService
{
    @Autowired
    private AppTrackMapper appTrackMapper;

    @Autowired
    private ICompositeEventService compositeEventService;

    @Autowired
    private EventTrackRelationMapper relationMapper;

    /**
     * 查询轨迹
     * 
     * @param id 轨迹主键
     * @return 轨迹
     */
    @Override
    public AppTrack selectAppTrackById(Long id)
    {
        return appTrackMapper.selectAppTrackById(id);
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
        return appTrackMapper.selectAppTrackList(appTrack);
    }

    /**
     * 新增轨迹
     * 应用层实时写入：插入轨迹后自动更新复合事件
     *
     * @param appTrack 轨迹
     * @return 结果
     */
    @Override
    public int insertAppTrack(AppTrack appTrack)
    {
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
        int result = appTrackMapper.updateAppTrack(appTrack);
        if (result > 0)
        {
            // 实时更新复合事件（方案B）
            compositeEventService.updateOrCreateCompositeEventByTrack(appTrack);
        }
        return result;
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

        // 传递其他筛选条件（区域、状态、人员等）
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
                    tracks.add(track);
                }
            }

            // 确保 events 字段始终有值（即使是空数组），避免前端报错
            event.setEvents(tracks);
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
