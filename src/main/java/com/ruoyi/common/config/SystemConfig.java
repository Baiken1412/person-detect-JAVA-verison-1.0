package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "system")
public class SystemConfig
{
    /** 系统标题 */
    private String title = "视频分析插件";

    /** 系统副标题 */
    private String subtitle = "人员活动自动识别与追溯管理系统";

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSubtitle()
    {
        return subtitle;
    }

    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }
}
