package com.ruoyi.project.caseapp.track.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 复合事件对象（持久化版本）
 * 基于30秒空闲检测算法，应用层实时写入
 *
 * @author ruoyi
 * @date 2025-12-23
 */
public class CompositeEvent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 事件ID（第一条轨迹的ID） */
    @Excel(name = "事件ID")
    private Long eventId;

    /** 主要区域ID */
    private Long qyid;

    /** 主要区域名称 */
    @Excel(name = "主要区域")
    private String qymc;

    /** 是否结束：0=进行中，1=已结束 */
    private Integer isClosed;

    /** 标注状态：0=待标注，1=已标注 */
    @Excel(name = "标注状态", readConverterExp = "0=待标注,1=已标注")
    private String bzzt;

    /** 事件开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 事件结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 持续时长（分钟） */
    @Excel(name = "持续时长(分钟)")
    private Integer duration;

    /** 包含轨迹数量 */
    @Excel(name = "轨迹数量")
    private Integer trackCount;

    /** 行为原因（聚合） */
    @Excel(name = "行为原因")
    private String xwyy;

    /** 人员姓名（聚合，逗号分隔） */
    @Excel(name = "人员姓名")
    private String ryxm;

    /** 外来人员（聚合，逗号分隔） */
    @Excel(name = "外来人员")
    private String wlry;

    /** 最大人员数量 */
    @Excel(name = "最大人数")
    private Integer ryslMax;

    /** 经过的区域路径（逗号分隔） */
    @Excel(name = "路径")
    private String pathAreas;

    /** 是否包含非工作时间：0=否，1=是 */
    private Integer hasNonworktime;

    /** 是否人员异常：0=否，1=是 */
    private Integer hasAbnormalPerson;

    /** 包含的轨迹ID列表（逗号分隔） */
    private String trackIds;

    /** 该复合事件包含的所有轨迹数据（不持久化，仅用于前端展示） */
    private transient List<AppTrack> events;

    /** 事件数量（兼容旧代码） */
    private Integer eventCount;

    public CompositeEvent()
    {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getQyid() {
        return qyid;
    }

    public void setQyid(Long qyid) {
        this.qyid = qyid;
    }

    public String getQymc() {
        return qymc;
    }

    public void setQymc(String qymc) {
        this.qymc = qymc;
    }

    public Integer getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Integer isClosed) {
        this.isClosed = isClosed;
    }

    public String getBzzt()
    {
        return bzzt;
    }

    public void setBzzt(String bzzt)
    {
        this.bzzt = bzzt;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(Integer trackCount) {
        this.trackCount = trackCount;
    }

    public String getXwyy() {
        return xwyy;
    }

    public void setXwyy(String xwyy) {
        this.xwyy = xwyy;
    }

    public String getRyxm() {
        return ryxm;
    }

    public void setRyxm(String ryxm) {
        this.ryxm = ryxm;
    }

    public String getWlry() {
        return wlry;
    }

    public void setWlry(String wlry) {
        this.wlry = wlry;
    }

    public Integer getRyslMax() {
        return ryslMax;
    }

    public void setRyslMax(Integer ryslMax) {
        this.ryslMax = ryslMax;
    }

    public String getPathAreas() {
        return pathAreas;
    }

    public void setPathAreas(String pathAreas) {
        this.pathAreas = pathAreas;
    }

    public Integer getHasNonworktime() {
        return hasNonworktime;
    }

    public void setHasNonworktime(Integer hasNonworktime) {
        this.hasNonworktime = hasNonworktime;
    }

    public Integer getHasAbnormalPerson() {
        return hasAbnormalPerson;
    }

    public void setHasAbnormalPerson(Integer hasAbnormalPerson) {
        this.hasAbnormalPerson = hasAbnormalPerson;
    }

    public String getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(String trackIds) {
        this.trackIds = trackIds;
    }

    public List<AppTrack> getEvents()
    {
        return events;
    }

    public void setEvents(List<AppTrack> events)
    {
        this.events = events;
        if (events != null)
        {
            this.eventCount = events.size();
            this.trackCount = events.size();
        }
    }

    public Integer getEventCount()
    {
        return eventCount;
    }

    public void setEventCount(Integer eventCount)
    {
        this.eventCount = eventCount;
    }
}

