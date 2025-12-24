package com.ruoyi.project.caseapp.track.mapper;

import java.util.List;
import com.ruoyi.project.caseapp.track.domain.AppTrack;

/**
 * 轨迹Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-11
 */
public interface AppTrackMapper 
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
     * 删除轨迹
     * 
     * @param id 轨迹主键
     * @return 结果
     */
    public int deleteAppTrackById(Long id);

    /**
     * 批量删除轨迹
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAppTrackByIds(String[] ids);

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
     * 查询所有轨迹数据（用于复合事件分组，支持时间筛选）
     * 
     * @param appTrack 轨迹（用于时间筛选）
     * @return 轨迹集合（按时间排序）
     */
    public List<AppTrack> selectAllAppTrackForComposite(AppTrack appTrack);
}
