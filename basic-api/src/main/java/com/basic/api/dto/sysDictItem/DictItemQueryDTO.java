package com.basic.api.dto.sysDictItem;

import lombok.Data;

/**
 * 字典项查询DTO
 *
 * @author Gas
 */
@Data
public class DictItemQueryDTO {

    /**
     * 字典ID
     */
    private Long dictId;

    /**
     * 值（模糊查询）
     */
    private String itemValue;

    /**
     * 标签（模糊查询）
     */
    private String itemLabel;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
