package com.ruoyi.project.caseapp.track.service;

import java.util.List;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.domain.AppTrack;

/**
 * 复合事件Service接口
 *
 * @author ruoyi
 * @date 2025-12-23
 */
public interface ICompositeEventService
{
    /**
     * 查询复合事件
     *
     * @param id 复合事件主键
     * @return 复合事件
     */
    public CompositeEvent selectCompositeEventById(Long id);

    /**
     * 查询复合事件列表
     *
     * @param compositeEvent 复合事件
     * @return 复合事件集合
     */
    public List<CompositeEvent> selectCompositeEventList(CompositeEvent compositeEvent);

    /**
     * 新增复合事件
     *
     * @param compositeEvent 复合事件
     * @return 结果
     */
    public int insertCompositeEvent(CompositeEvent compositeEvent);

    /**
     * 修改复合事件
     *
     * @param compositeEvent 复合事件
     * @return 结果
     */
    public int updateCompositeEvent(CompositeEvent compositeEvent);

    /**
     * 批量删除复合事件
     *
     * @param ids 需要删除的复合事件主键集合
     * @return 结果
     */
    public int deleteCompositeEventByIds(String ids);

    /**
     * 删除复合事件信息
     *
     * @param id 复合事件主键
     * @return 结果
     */
    public int deleteCompositeEventById(Long id);

    /**
     * 根据轨迹更新或创建复合事件（核心方法）
     * 应用层实时写入：当轨迹数据变化时，自动计算并更新复合事件
     *
     * @param track 新增或修改的轨迹
     */
    public void updateOrCreateCompositeEventByTrack(AppTrack track);

    /**
     * 重新计算并同步所有复合事件
     * 用于初始化或数据修复
     *
     * @param appTrack 查询条件（时间范围等）
     * @return 同步的事件数量
     */
    public int syncAllCompositeEvents(AppTrack appTrack);

    /**
     * 标注复合事件
     * 标注复合事件时，会同时更新该事件下的所有轨迹
     *
     * @param eventId 复合事件的event_id（第一条轨迹的ID）
     * @param xwyy 行为原因
     * @param ryxm 人员姓名
     * @param wlry 外来人员
     * @param remark 备注
     */
    public void annotateCompositeEvent(Long eventId, String xwyy, String ryxm, String wlry, String remark);

    /**
     * 根据eventId查询复合事件
     *
     * @param eventId 事件ID（第一条轨迹的ID）
     * @return 复合事件
     */
    public CompositeEvent selectCompositeEventByEventId(Long eventId);

    /**
     * 处理轨迹删除后的复合事件清理
     * 应用层实时写入：删除轨迹后自动删除包含该轨迹的所有复合事件
     *
     * @param track 被删除的轨迹
     */
    public void handleTrackDeletion(AppTrack track);

    /**
     * 导出事件包（HTML报告 + 图片 + 视频 + JSON数据）
     * 生成包含事件完整信息的离线可查看文件夹
     *
     * @param eventId 复合事件ID
     * @param exportPath 导出路径
     * @return 导出路径
     */
    public String exportEventPackage(Long eventId, String exportPath) throws Exception;

    /**
     * 批量导出事件包（多个事件导出到一个文件夹）
     *
     * @param eventIds 复合事件ID列表
     * @param exportPath 导出路径
     * @return 导出文件夹路径
     */
    public String batchExportEventPackages(List<Long> eventIds, String exportPath) throws Exception;

    /**
     * 统计今日复合事件总数
     */
    public int countTodayEvents();

    /**
     * 统计待标注复合事件数量
     */
    public int countUnlabeledEvents();

    /**
     * 统计已标注复合事件数量
     */
    public int countLabeledEvents();

    /**
     * 统计本月复合事件总数
     */
    public int countMonthEvents();
}
