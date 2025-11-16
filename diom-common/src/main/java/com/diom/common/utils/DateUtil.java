package com.diom.common.utils;

import com.diom.common.constant.CommonConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    private DateUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 格式化日期时间
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATETIME_PATTERN);
        return sdf.format(date);
    }

    /**
     * 格式化日期
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_PATTERN);
        return sdf.format(date);
    }

    /**
     * 解析日期时间字符串
     */
    public static Date parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATETIME_PATTERN);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr, e);
        }
    }

    /**
     * 解析日期字符串
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATE_PATTERN);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr, e);
        }
    }

    /**
     * 获取当前日期时间字符串
     */
    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }

    /**
     * 获取当前日期字符串
     */
    public static String getCurrentDate() {
        return formatDate(new Date());
    }

    /**
     * Date 转 LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 格式化 LocalDateTime
     */
    public static String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstant.DATETIME_PATTERN);
        return dateTime.format(formatter);
    }

    /**
     * 解析 LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CommonConstant.DATETIME_PATTERN);
        return LocalDateTime.parse(dateStr, formatter);
    }
}

