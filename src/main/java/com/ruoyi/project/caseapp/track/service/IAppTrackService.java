package com.ruoyi.project.caseapp.track.service;

import java.util.List;

import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.domain.AppTrack;

/**
 * 轨迹Service接口
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public interface IAppTrackService 
{
    /**
     * 查询轨迹
     * 
     * @param id 轨迹主键
     * @return 轨迹
     */
    public AppTrack selectAppTrackById(Long id);

    /**
     * 查询轨迹列表
     * 
     * @param appTrack 轨迹
     * @return 轨迹集合
     */
    public List<AppTrack> selectAppTrackList(AppTrack appTrack);

    /**
     * 新增轨迹
     * 
     * @param appTrack 轨迹
     * @return 结果
     */
    public int insertAppTrack(AppTrack appTrack);

    /**
     * 修改轨迹
     * 
     * @param appTrack 轨迹
     * @return 结果
     */
    public int updateAppTrack(AppTrack appTrack);

    /**
     * 批量删除轨迹
     * 
     * @param ids 需要删除的轨迹主键集合
     * @return 结果
     */
    public int deleteAppTrackByIds(String ids);

    /**
     * 删除轨迹信息
     * 
     * @param id 轨迹主键
     * @return 结果
     */
    public int deleteAppTrackById(Long id);

    /**
     * 统计今日事件总数
     * 
     * @return 今日事件总数
     */
    public int countTodayEvents();

    /**
     * 统计待标注事件数（bzzt = "0"）
     * 
     * @return 待标注事件数
     */
    public int countUnlabeledEvents();

    /**
     * 统计已标注事件数（bzzt = "1"）
     * 
     * @return 已标注事件数
     */
    public int countLabeledEvents();

    /**
     * 统计本月事件总数
     * 
     * @return 本月事件总数
     */
    public int countMonthEvents();

    /**
     * 查询复合事件列表
     * qyid = 1 表示复合事件的标记点，两个标记点之间的所有数据组成一个复合事件
     * 
     * @param appTrack 轨迹（用于时间筛选）
     * @return 复合事件集合
     */
    public List<CompositeEvent> selectCompositeEvents(AppTrack appTrack);
}
