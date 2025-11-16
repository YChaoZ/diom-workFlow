package com.diom.common.constant;

/**
 * 通用常量
 */
public class CommonConstant {

    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 成功标识
     */
    public static final Integer SUCCESS = 1;

    /**
     * 失败标识
     */
    public static final Integer FAIL = 0;

    /**
     * 是
     */
    public static final String YES = "Y";

    /**
     * 否
     */
    public static final String NO = "N";

    /**
     * Token 请求头
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户 ID Header
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名 Header
     */
    public static final String USERNAME_HEADER = "X-Username";

    /**
     * 日期时间格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    private CommonConstant() {
        throw new IllegalStateException("Constant class");
    }
}

