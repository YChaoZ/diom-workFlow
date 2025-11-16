package com.diom.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * 字符串工具类
 */
public class StringUtil {

    private StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    /**
     * 判断字符串是否为空白
     */
    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * 判断字符串是否非空白
     */
    public static boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    /**
     * 去除字符串两端空白
     */
    public static String trim(String str) {
        return StringUtils.trim(str);
    }

    /**
     * 生成 UUID（去除连字符）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成带连字符的 UUID
     */
    public static String uuidWithHyphen() {
        return UUID.randomUUID().toString();
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        return StringUtils.uncapitalize(str);
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderscore(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        result.append(str.charAt(0));
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean toUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '_') {
                toUpper = true;
            } else {
                if (toUpper) {
                    result.append(Character.toUpperCase(ch));
                    toUpper = false;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * 格式化字符串（占位符 {}）
     */
    public static String format(String template, Object... params) {
        if (isBlank(template) || params == null || params.length == 0) {
            return template;
        }
        StringBuilder result = new StringBuilder();
        int paramIndex = 0;
        int i = 0;
        while (i < template.length()) {
            if (i < template.length() - 1 && template.charAt(i) == '{' && template.charAt(i + 1) == '}') {
                if (paramIndex < params.length) {
                    result.append(params[paramIndex++]);
                }
                i += 2;
            } else {
                result.append(template.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    /**
     * 判断字符串是否包含中文
     */
    public static boolean containsChinese(String str) {
        if (isBlank(str)) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }
}

