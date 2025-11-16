package com.diom.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        this.pages = (int) ((total + pageSize - 1) / pageSize);
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }
}

