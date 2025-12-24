package com.ruoyi.project.caseapp.track.mapper;

import java.util.List;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;

/**
 * 复合事件Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-23
 */
public interface CompositeEventMapper
{
    /**
     * 查询复合事件
     *
     * @param id 复合事件主键
     * @return 复合事件
     */
    public CompositeEvent selectCompositeEventById(Long id);

    /**
     * 根据事件ID查询复合事件
     *
     * @param eventId 事件ID
     * @return 复合事件
     */
    public CompositeEvent selectCompositeEventByEventId(Long eventId);

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
     * 删除复合事件
     *
     * @param id 复合事件主键
     * @return 结果
     */
    public int deleteCompositeEventById(Long id);

    /**
     * 批量删除复合事件
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompositeEventByIds(String[] ids);

    /**
     * 根据轨迹ID查询包含该轨迹的复合事件
     * 已废弃：现在使用关系表 EventTrackRelationMapper.selectEventIdsByTrackId()
     *
     * @param trackId 轨迹ID
     * @return 复合事件列表
     */
    // @Deprecated
    // public List<CompositeEvent> selectCompositeEventsByTrackId(Long trackId);

    /**
     * 关闭复合事件（设置is_closed=1）
     *
     * @param id 复合事件主键
     * @return 结果
     */
    public int closeCompositeEvent(Long id);
}
