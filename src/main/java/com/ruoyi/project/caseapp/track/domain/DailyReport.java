package com.ruoyi.project.caseapp.track.domain;

import java.util.Date;
import java.util.List;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 每日情况报告
 *
 * @author ruoyi
 * @date 2025-12-24
 */
public class DailyReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 报告日期 */
    private Date reportDate;

    /** 总体判断：normal(正常), attention(需要关注), abnormal(存在异常) */
    private String overallStatus;

    /** 总体判断说明 */
    private String overallDescription;

    /** 总事件数 */
    private Integer totalEvents;

    /** 异常事件数 */
    private Integer abnormalEvents;

    /** 可疑事件数（待标注） */
    private Integer suspiciousEvents;

    /** 正常事件数 */
    private Integer normalEvents;

    /** 异常事件列表（需要关注的事件） */
    private List<CompositeEvent> abnormalEventList;

    /** 可疑事件列表（待标注的事件） */
    private List<CompositeEvent> suspiciousEventList;

    /** 管理建议列表 */
    private List<String> managementSuggestions;

    /** 今日系统提示 */
    private List<String> systemTips;

    // Getters and Setters

    public Date getReportDate()
    {
        return reportDate;
    }

    public void setReportDate(Date reportDate)
    {
        this.reportDate = reportDate;
    }

    public String getOverallStatus()
    {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus)
    {
        this.overallStatus = overallStatus;
    }

    public String getOverallDescription()
    {
        return overallDescription;
    }

    public void setOverallDescription(String overallDescription)
    {
        this.overallDescription = overallDescription;
    }

    public Integer getTotalEvents()
    {
        return totalEvents;
    }

    public void setTotalEvents(Integer totalEvents)
    {
        this.totalEvents = totalEvents;
    }

    public Integer getAbnormalEvents()
    {
        return abnormalEvents;
    }

    public void setAbnormalEvents(Integer abnormalEvents)
    {
        this.abnormalEvents = abnormalEvents;
    }

    public Integer getSuspiciousEvents()
    {
        return suspiciousEvents;
    }

    public void setSuspiciousEvents(Integer suspiciousEvents)
    {
        this.suspiciousEvents = suspiciousEvents;
    }

    public Integer getNormalEvents()
    {
        return normalEvents;
    }

    public void setNormalEvents(Integer normalEvents)
    {
        this.normalEvents = normalEvents;
    }

    public List<CompositeEvent> getAbnormalEventList()
    {
        return abnormalEventList;
    }

    public void setAbnormalEventList(List<CompositeEvent> abnormalEventList)
    {
        this.abnormalEventList = abnormalEventList;
    }

    public List<CompositeEvent> getSuspiciousEventList()
    {
        return suspiciousEventList;
    }

    public void setSuspiciousEventList(List<CompositeEvent> suspiciousEventList)
    {
        this.suspiciousEventList = suspiciousEventList;
    }

    public List<String> getManagementSuggestions()
    {
        return managementSuggestions;
    }

    public void setManagementSuggestions(List<String> managementSuggestions)
    {
        this.managementSuggestions = managementSuggestions;
    }

    public List<String> getSystemTips()
    {
        return systemTips;
    }

    public void setSystemTips(List<String> systemTips)
    {
        this.systemTips = systemTips;
    }
}
