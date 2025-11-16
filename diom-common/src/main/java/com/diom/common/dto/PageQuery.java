package com.diom.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderDirection = "ASC";

    /**
     * 获取偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 校验并修正分页参数
     */
    public void validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }
    }
}

