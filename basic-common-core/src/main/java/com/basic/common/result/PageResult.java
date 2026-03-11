package com.basic.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 *
 * @param <T> 数据泛型
 * @author Gas
 */
@Data
public class PageResult<T> {

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页大小
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long pageNum, Long pageSize, Long total, Long pages, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
        this.list = list;
    }

    /**
     * 构建分页结果
     *
     * @param pageNum   当前页码
     * @param pageSize  每页大小
     * @param total     总记录数
     * @param list      数据列表
     * @param <T>       数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long pageNum, Long pageSize, Long total, List<T> list) {
        long pages = (total + pageSize - 1) / pageSize;
        return new PageResult<>(pageNum, pageSize, total, pages, list);
    }
}
