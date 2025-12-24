package com.ruoyi.project.caseapp.track.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 复合事件与轨迹关系对象
 *
 * @author ruoyi
 * @date 2025-12-24
 */
public class EventTrackRelation
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增 */
    private Long id;

    /** 复合事件ID，关联app_composite_event.id */
    private Long eventId;

    /** 轨迹ID，关联app_track.id */
    private Long trackId;

    /** 轨迹在事件中的顺序号（从1开始） */
    private Integer seqNo;

    /** 关联建立时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    public EventTrackRelation()
    {
    }

    public EventTrackRelation(Long eventId, Long trackId, Integer seqNo)
    {
        this.eventId = eventId;
        this.trackId = trackId;
        this.seqNo = seqNo;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getEventId()
    {
        return eventId;
    }

    public void setEventId(Long eventId)
    {
        this.eventId = eventId;
    }

    public Long getTrackId()
    {
        return trackId;
    }

    public void setTrackId(Long trackId)
    {
        this.trackId = trackId;
    }

    public Integer getSeqNo()
    {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo)
    {
        this.seqNo = seqNo;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    @Override
    public String toString()
    {
        return "EventTrackRelation{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", trackId=" + trackId +
                ", seqNo=" + seqNo +
                ", createdAt=" + createdAt +
                '}';
    }
}
