package com.diom.common.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
public enum StatusEnum {

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 删除
     */
    DELETED(-1, "已删除");

    private final Integer code;
    private final String description;

    StatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static StatusEnum of(Integer code) {
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

