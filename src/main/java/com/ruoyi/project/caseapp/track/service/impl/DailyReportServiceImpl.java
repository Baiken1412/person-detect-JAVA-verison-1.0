package com.ruoyi.project.caseapp.track.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.caseapp.track.domain.CompositeEvent;
import com.ruoyi.project.caseapp.track.domain.DailyReport;
import com.ruoyi.project.caseapp.track.service.ICompositeEventService;
import com.ruoyi.project.caseapp.track.service.IDailyReportService;

/**
 * 每日报告Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-24
 */
@Service
public class DailyReportServiceImpl implements IDailyReportService
{
    @Autowired
    private ICompositeEventService compositeEventService;

    /**
     * 生成指定日期的每日报告
     *
     * @param date 报告日期（如果为null，则生成今天的报告）
     * @return 每日报告
     */
    @Override
    public DailyReport generateDailyReport(Date date)
    {
        // 如果没有指定日期，使用今天
        if (date == null)
        {
            date = new Date();
        }

        DailyReport report = new DailyReport();
        report.setReportDate(date);

        // 设置时间范围：当天00:00:00 ~ 23:59:59
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date beginTime = cal.getTime();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endTime = cal.getTime();

        // 查询当天的所有复合事件
        CompositeEvent queryParam = new CompositeEvent();
        queryParam.getParams().put("beginTime", beginTime);
        queryParam.getParams().put("endTime", endTime);

        List<CompositeEvent> allEvents = compositeEventService.selectCompositeEventList(queryParam);

        // 分类事件
        List<CompositeEvent> abnormalEvents = new ArrayList<>();
        List<CompositeEvent> suspiciousEvents = new ArrayList<>();
        List<CompositeEvent> normalEvents = new ArrayList<>();

        for (CompositeEvent event : allEvents)
        {
            boolean isAbnormal = isAbnormalEvent(event);
            boolean isSuspicious = isSuspiciousEvent(event);

            // 异常事件
            if (isAbnormal)
            {
                abnormalEvents.add(event);
            }

            // 待标注事件（可能与异常事件重复，即未标注的异常事件）
            if (isSuspicious)
            {
                suspiciousEvents.add(event);
            }

            // 正常事件（既不异常也不待标注）
            if (!isAbnormal && !isSuspicious)
            {
                normalEvents.add(event);
            }
        }

        // 设置统计数据
        report.setTotalEvents(allEvents.size());
        report.setAbnormalEvents(abnormalEvents.size());
        report.setSuspiciousEvents(suspiciousEvents.size());
        report.setNormalEvents(normalEvents.size());

        // 设置事件列表（按时间倒序）
        report.setAbnormalEventList(abnormalEvents.stream()
                .sorted(Comparator.comparing(CompositeEvent::getStartTime).reversed())
                .collect(Collectors.toList()));

        report.setSuspiciousEventList(suspiciousEvents.stream()
                .sorted(Comparator.comparing(CompositeEvent::getStartTime).reversed())
                .limit(10) // 最多显示10个待标注事件
                .collect(Collectors.toList()));

        // 生成总体判断
        generateOverallStatus(report, abnormalEvents, suspiciousEvents);

        // 生成管理建议
        generateManagementSuggestions(report, abnormalEvents, suspiciousEvents);

        // 生成系统提示
        generateSystemTips(report, allEvents);

        return report;
    }

    /**
     * 判断是否为异常事件
     */
    private boolean isAbnormalEvent(CompositeEvent event)
    {
        // 非工作时间出现
        if (event.getHasNonworktime() != null && event.getHasNonworktime() == 1)
        {
            return true;
        }

        // 多人同时出现
        if (event.getHasAbnormalPerson() != null && event.getHasAbnormalPerson() == 1)
        {
            return true;
        }

        // 外来人员进入
        if (StringUtils.isNotEmpty(event.getWlry()))
        {
            return true;
        }

        // 持续时间异常长（超过30分钟，即1800秒）
        if (event.getDuration() != null && event.getDuration() > 1800)
        {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为可疑事件（待标注）
     */
    private boolean isSuspiciousEvent(CompositeEvent event)
    {
        return "0".equals(event.getBzzt());
    }

    /**
     * 生成总体判断
     */
    private void generateOverallStatus(DailyReport report, List<CompositeEvent> abnormalEvents, List<CompositeEvent> suspiciousEvents)
    {
        int abnormalCount = abnormalEvents.size();
        int suspiciousCount = suspiciousEvents.size();

        // 统计未标注的异常事件
        long unlabeledAbnormalCount = abnormalEvents.stream()
                .filter(e -> "0".equals(e.getBzzt()))
                .count();

        String status;
        String description;

        if (abnormalCount == 0 && suspiciousCount == 0)
        {
            // 全部正常
            status = "normal";
            description = "今日共检测到 " + report.getTotalEvents() + " 个事件，全部为正常事件，无需关注";
        }
        else if (abnormalCount <= 2 && suspiciousCount <= 3 && unlabeledAbnormalCount == 0)
        {
            // 基本正常
            status = "normal";
            description = "今日共检测到 " + report.getTotalEvents() + " 个事件，其中 " +
                    abnormalCount + " 个异常事件已处理，" +
                    suspiciousCount + " 个事件待标注，整体情况基本正常";
        }
        else if (abnormalCount >= 5 || (abnormalCount > 0 && unlabeledAbnormalCount > 0))
        {
            // 存在异常
            status = "abnormal";
            description = "今日共检测到 " + report.getTotalEvents() + " 个事件，其中 " +
                    abnormalCount + " 个异常事件";
            if (unlabeledAbnormalCount > 0)
            {
                description += "（" + unlabeledAbnormalCount + " 个未处理）";
            }
            description += "，需要重点关注";
        }
        else
        {
            // 需要关注
            status = "attention";
            description = "今日共检测到 " + report.getTotalEvents() + " 个事件，其中 " +
                    abnormalCount + " 个异常事件，" +
                    suspiciousCount + " 个待标注事件，建议关注";
        }

        report.setOverallStatus(status);
        report.setOverallDescription(description);
    }

    /**
     * 生成管理建议
     */
    private void generateManagementSuggestions(DailyReport report, List<CompositeEvent> abnormalEvents, List<CompositeEvent> suspiciousEvents)
    {
        List<String> suggestions = new ArrayList<>();

        // 建议1：待标注事件
        if (suspiciousEvents.size() > 0)
        {
            suggestions.add("建议尽快标注 " + suspiciousEvents.size() + " 个待处理事件");
        }

        // 建议2：外来人员
        long outsiderCount = abnormalEvents.stream()
                .filter(e -> StringUtils.isNotEmpty(e.getWlry()))
                .count();
        if (outsiderCount > 0)
        {
            suggestions.add("有 " + outsiderCount + " 个外来人员访问记录，建议核实访问原因");
        }

        // 建议3：非工作时间活动
        long nonworktimeCount = abnormalEvents.stream()
                .filter(e -> e.getHasNonworktime() != null && e.getHasNonworktime() == 1)
                .count();
        if (nonworktimeCount > 0)
        {
            suggestions.add("有 " + nonworktimeCount + " 个非工作时间活动记录，建议检查是否为正常加班或巡检");
        }

        // 建议4：长时间活动
        long longDurationCount = abnormalEvents.stream()
                .filter(e -> e.getDuration() != null && e.getDuration() > 1800)
                .count();
        if (longDurationCount > 0)
        {
            suggestions.add("有 " + longDurationCount + " 个持续时间较长的活动（>30分钟），建议核查");
        }

        // 如果没有特别建议，给出正常提示
        if (suggestions.isEmpty())
        {
            suggestions.add("今日整体情况正常，继续保持");
        }

        report.setManagementSuggestions(suggestions);
    }

    /**
     * 生成系统提示
     */
    private void generateSystemTips(DailyReport report, List<CompositeEvent> allEvents)
    {
        List<String> tips = new ArrayList<>();

        if (allEvents.isEmpty())
        {
            tips.add("今日暂无检测到活动事件");
        }
        else
        {
            // 提示1：数据统计
            tips.add("数据统计时间范围：当日00:00 - 23:59");

            // 提示2：异常判定规则
            tips.add("异常事件判定规则：非工作时间活动、多人同时出现、外来人员进入、持续时间>1800秒（30分钟）");

            // 提示3：建议操作
            if (report.getSuspiciousEvents() > 0)
            {
                tips.add("建议及时标注待处理事件，以便系统更准确地进行分析");
            }
        }

        report.setSystemTips(tips);
    }
}
