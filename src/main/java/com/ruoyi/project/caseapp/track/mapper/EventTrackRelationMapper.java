package com.ruoyi.project.caseapp.track.mapper;

import com.ruoyi.project.caseapp.track.domain.EventTrackRelation;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 复合事件与轨迹关系Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-24
 */
public interface EventTrackRelationMapper
{
    /**
     * 查询关系列表
     *
     * @param relation 查询条件
     * @return 关系列表
     */
    public List<EventTrackRelation> selectRelationList(EventTrackRelation relation);

    /**
     * 根据事件ID查询所有轨迹ID
     *
     * @param eventId 事件ID
     * @return 轨迹ID列表
     */
    public List<Long> selectTrackIdsByEventId(@Param("eventId") Long eventId);

    /**
     * 根据轨迹ID查询所有事件ID
     *
     * @param trackId 轨迹ID
     * @return 事件ID列表
     */
    public List<Long> selectEventIdsByTrackId(@Param("trackId") Long trackId);

    /**
     * 批量插入关系
     *
     * @param relations 关系列表
     * @return 插入数量
     */
    public int batchInsertRelations(@Param("relations") List<EventTrackRelation> relations);

    /**
     * 插入单个关系
     *
     * @param relation 关系
     * @return 插入数量
     */
    public int insertRelation(EventTrackRelation relation);

    /**
     * 删除事件的所有关系
     *
     * @param eventId 事件ID
     * @return 删除数量
     */
    public int deleteByEventId(@Param("eventId") Long eventId);

    /**
     * 删除轨迹的所有关系
     *
     * @param trackId 轨迹ID
     * @return 删除数量
     */
    public int deleteByTrackId(@Param("trackId") Long trackId);

    /**
     * 删除指定的关系
     *
     * @param eventId 事件ID
     * @param trackId 轨迹ID
     * @return 删除数量
     */
    public int deleteRelation(@Param("eventId") Long eventId, @Param("trackId") Long trackId);

    /**
     * 统计事件包含的轨迹数量
     *
     * @param eventId 事件ID
     * @return 轨迹数量
     */
    public int countTracksByEventId(@Param("eventId") Long eventId);
}
