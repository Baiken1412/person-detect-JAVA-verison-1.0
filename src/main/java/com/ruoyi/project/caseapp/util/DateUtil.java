package com.ruoyi.project.caseapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 线程安全的日期工具类
 * 使用Java 8 DateTimeFormatter替代SimpleDateFormat
 *
 * @author ruoyi
 */
public class DateUtil {

    /**
     * 常用日期格式化器（线程安全）
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter TIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter COMPACT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter COMPACT_DATETIME_T_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * 格式化日期时间为 yyyy-MM-dd HH:mm:ss
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期为 yyyy-MM-dd
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate localDate = dateToLocalDate(date);
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * 格式化时间为 HH:mm:ss
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * 格式化时间为 HH:mm
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatTimeShort(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        return dateTime.format(TIME_SHORT_FORMATTER);
    }

    /**
     * 格式化日期时间为 yyyyMMdd_HHmmss（文件名专用）
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatCompactDateTime(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        return dateTime.format(COMPACT_DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为 yyyyMMddTHHmmss
     *
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatCompactDateTimeT(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        return dateTime.format(COMPACT_DATETIME_T_FORMATTER);
    }

    /**
     * 使用自定义格式化器格式化日期
     *
     * @param date 日期对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null || pattern == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = dateToLocalDateTime(date);
        return dateTime.format(formatter);
    }

    /**
     * 解析日期字符串 yyyy-MM-dd
     *
     * @param dateStr 日期字符串
     * @return Date对象
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return localDateToDate(localDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式不正确，期望格式: yyyy-MM-dd，实际: " + dateStr, e);
        }
    }

    /**
     * 解析日期时间字符串 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTimeStr 日期时间字符串
     * @return Date对象
     */
    public static Date parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr.trim(), DATETIME_FORMATTER);
            return localDateTimeToDate(localDateTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("日期时间格式不正确，期望格式: yyyy-MM-dd HH:mm:ss，实际: " + dateTimeStr, e);
        }
    }

    /**
     * 使用自定义格式解析日期字符串
     *
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return Date对象
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty() || pattern == null) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            // 尝试解析为LocalDateTime
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateStr.trim(), formatter);
                return localDateTimeToDate(localDateTime);
            } catch (Exception e1) {
                // 如果失败，尝试解析为LocalDate
                LocalDate localDate = LocalDate.parse(dateStr.trim(), formatter);
                return localDateToDate(localDate);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式不正确，期望格式: " + pattern + "，实际: " + dateStr, e);
        }
    }

    /**
     * Date转LocalDateTime
     */
    private static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Date转LocalDate
     */
    private static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * LocalDateTime转Date
     */
    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate转Date
     */
    private static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取当前日期时间字符串 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前日期时间字符串
     */
    public static String now() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 获取今日日期字符串 yyyy-MM-dd
     *
     * @return 今日日期字符串
     */
    public static String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
