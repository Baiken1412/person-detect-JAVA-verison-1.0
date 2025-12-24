package com.ruoyi.project.caseapp.track.service;

import java.util.Date;
import com.ruoyi.project.caseapp.track.domain.DailyReport;

/**
 * 每日报告Service接口
 *
 * @author ruoyi
 * @date 2025-12-24
 */
public interface IDailyReportService
{
    /**
     * 生成指定日期的每日报告
     *
     * @param date 报告日期（如果为null，则生成今天的报告）
     * @return 每日报告
     */
    public DailyReport generateDailyReport(Date date);
}
