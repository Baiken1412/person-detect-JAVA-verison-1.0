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

        // 边界检查：确保返回的不是null
        if (allEvents == null) {
            allEvents = new ArrayList<>();
        }

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

        // 设置事件列表（按时间倒序，处理null值）
        report.setAbnormalEventList(abnormalEvents.stream()
                .sorted(Comparator.comparing(CompositeEvent::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList()));

        report.setSuspiciousEventList(suspiciousEvents.stream()
                .sorted(Comparator.comparing(CompositeEvent::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10) // 最多显示10个待标注事件
                .collect(Collectors.toList()));

        // 设置正常事件列表（展示前5条）
        report.setNormalEventList(normalEvents.stream()
                .sorted(Comparator.comparing(CompositeEvent::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5) // 只显示5条正常事件示例
                .collect(Collectors.toList()));

        // 生成总体判断
        generateOverallStatus(report, abnormalEvents, suspiciousEvents);

        // 生成管理建议
        generateManagementSuggestions(report, abnormalEvents, suspiciousEvents);

        // 生成系统提示（增强版：包含历史对比、趋势分析、区域热度）
        generateSystemTips(report, allEvents, date);

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

        // 建议3：非工作时间活动（工作时间为9:00-17:00）
        long nonworktimeCount = abnormalEvents.stream()
                .filter(e -> e.getHasNonworktime() != null && e.getHasNonworktime() == 1)
                .count();
        if (nonworktimeCount > 0)
        {
            suggestions.add("有 " + nonworktimeCount + " 个非工作时间活动记录（工作时间：9:00-17:00），建议检查是否为正常加班或巡检");
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
     * 生成系统提示（增强版：历史对比、趋势分析、区域热度）
     */
    private void generateSystemTips(DailyReport report, List<CompositeEvent> allEvents, Date currentDate)
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
            tips.add("异常事件判定规则：非工作时间活动（工作时间为9:00-17:00）、多人同时出现、外来人员进入、持续时间>30分钟");

            // 提示3：历史对比
            String historyComparison = generateHistoryComparison(currentDate, report.getAbnormalEvents());
            if (historyComparison != null)
            {
                tips.add(historyComparison);
            }

            // 提示4：趋势分析
            String trendAnalysis = generateTrendAnalysis(currentDate);
            if (trendAnalysis != null)
            {
                tips.add(trendAnalysis);
            }

            // 提示5：区域热度
            String areaHotspot = generateAreaHotspot(allEvents);
            if (areaHotspot != null)
            {
                tips.add(areaHotspot);
            }

            // 提示6：建议操作
            if (report.getSuspiciousEvents() > 0)
            {
                tips.add("建议及时标注待处理事件，以便系统更准确地进行分析");
            }
        }

        report.setSystemTips(tips);
    }

    /**
     * 生成历史对比信息（直接查询数据库，避免递归）
     */
    private String generateHistoryComparison(Date currentDate, int todayAbnormalCount)
    {
        try
        {
            // 计算昨日时间范围
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date yesterdayBegin = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            Date yesterdayEnd = cal.getTime();

            // 直接查询昨日的复合事件
            CompositeEvent queryParam = new CompositeEvent();
            queryParam.getParams().put("beginTime", yesterdayBegin);
            queryParam.getParams().put("endTime", yesterdayEnd);
            List<CompositeEvent> yesterdayEvents = compositeEventService.selectCompositeEventList(queryParam);

            // 统计昨日异常事件数量
            int yesterdayAbnormalCount = 0;
            for (CompositeEvent event : yesterdayEvents)
            {
                if (isAbnormalEvent(event))
                {
                    yesterdayAbnormalCount++;
                }
            }

            int diff = todayAbnormalCount - yesterdayAbnormalCount;
            if (diff > 0)
            {
                return String.format("与昨日相比：异常事件增加%d个", diff);
            }
            else if (diff < 0)
            {
                return String.format("与昨日相比：异常事件减少%d个", Math.abs(diff));
            }
            else
            {
                return "与昨日相比：异常事件数量持平";
            }
        }
        catch (Exception e)
        {
            return null; // 如果查询失败，不显示对比信息
        }
    }

    /**
     * 生成趋势分析（直接查询数据库，避免递归）
     */
    private String generateTrendAnalysis(Date currentDate)
    {
        try
        {
            // 查询最近7天的异常事件数量
            List<Integer> weeklyData = new ArrayList<>();
            Calendar cal = Calendar.getInstance();

            for (int i = 6; i >= 0; i--)
            {
                cal.setTime(currentDate);
                cal.add(Calendar.DAY_OF_MONTH, -i);

                // 设置当天的开始和结束时间
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date dayBegin = cal.getTime();

                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Date dayEnd = cal.getTime();

                // 直接查询当天的复合事件
                CompositeEvent queryParam = new CompositeEvent();
                queryParam.getParams().put("beginTime", dayBegin);
                queryParam.getParams().put("endTime", dayEnd);
                List<CompositeEvent> dayEvents = compositeEventService.selectCompositeEventList(queryParam);

                // 统计异常事件数量
                int abnormalCount = 0;
                for (CompositeEvent event : dayEvents)
                {
                    if (isAbnormalEvent(event))
                    {
                        abnormalCount++;
                    }
                }
                weeklyData.add(abnormalCount);
            }

            // 简单趋势判断：前3天平均 vs 后4天平均
            double firstHalfAvg = weeklyData.subList(0, 3).stream().mapToInt(Integer::intValue).average().orElse(0);
            double secondHalfAvg = weeklyData.subList(3, 7).stream().mapToInt(Integer::intValue).average().orElse(0);

            if (secondHalfAvg > firstHalfAvg * 1.2)
            {
                return "本周趋势：异常事件呈上升趋势（较上周增加20%以上）";
            }
            else if (secondHalfAvg < firstHalfAvg * 0.8)
            {
                return "本周趋势：异常事件呈下降趋势（较上周减少20%以上）";
            }
            else
            {
                return "本周趋势：异常事件数量基本稳定";
            }
        }
        catch (Exception e)
        {
            return null; // 如果分析失败，不显示趋势信息
        }
    }

    /**
     * 生成区域热度统计
     */
    private String generateAreaHotspot(List<CompositeEvent> allEvents)
    {
        try
        {
            // 统计各区域的活动次数
            Map<String, Long> areaCountMap = allEvents.stream()
                    .filter(e -> StringUtils.isNotEmpty(e.getQymc()))
                    .collect(Collectors.groupingBy(CompositeEvent::getQymc, Collectors.counting()));

            if (areaCountMap.isEmpty())
            {
                return null;
            }

            // 找出活动最频繁的区域
            String hotspotArea = areaCountMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (hotspotArea != null)
            {
                long count = areaCountMap.get(hotspotArea);
                return String.format("活动热点区域：%s（共%d个事件）", hotspotArea, count);
            }

            return null;
        }
        catch (Exception e)
        {
            return null; // 如果统计失败，不显示热点信息
        }
    }
}
