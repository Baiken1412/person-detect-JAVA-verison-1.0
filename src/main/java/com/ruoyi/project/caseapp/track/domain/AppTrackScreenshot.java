package com.ruoyi.project.caseapp.track.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;

/**
 * 活动轨迹截图对象 app_track_screenshot
 *
 * @author ruoyi
 * @date 2025-12-31
 */
public class AppTrackScreenshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 活动轨迹ID（关联app_track.id） */
    @Excel(name = "活动轨迹ID")
    private Long trackId;

    /** 截图URL路径 */
    @Excel(name = "截图URL路径")
    private String screenshotUrl;

    /** 截图时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "截图时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date screenshotTime;

    /** 截图顺序（第几次检测） */
    @Excel(name = "截图顺序")
    private Integer screenshotOrder;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setTrackId(Long trackId)
    {
        this.trackId = trackId;
    }

    public Long getTrackId()
    {
        return trackId;
    }

    public void setScreenshotUrl(String screenshotUrl)
    {
        this.screenshotUrl = screenshotUrl;
    }

    public String getScreenshotUrl()
    {
        return screenshotUrl;
    }

    public void setScreenshotTime(Date screenshotTime)
    {
        this.screenshotTime = screenshotTime;
    }

    public Date getScreenshotTime()
    {
        return screenshotTime;
    }

    public void setScreenshotOrder(Integer screenshotOrder)
    {
        this.screenshotOrder = screenshotOrder;
    }

    public Integer getScreenshotOrder()
    {
        return screenshotOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("trackId", getTrackId())
            .append("screenshotUrl", getScreenshotUrl())
            .append("screenshotTime", getScreenshotTime())
            .append("screenshotOrder", getScreenshotOrder())
            .append("createTime", getCreateTime())
            .toString();
    }
}
