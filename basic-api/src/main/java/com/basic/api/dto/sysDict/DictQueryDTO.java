package com.basic.api.dto.sysDict;

import lombok.Data;

/**
 * 字典查询DTO
 *
 * @author Gas
 */
@Data
public class DictQueryDTO {

    /**
     * 字典编码（模糊查询）
     */
    private String dictCode;

    /**
     * 字典名称（模糊查询）
     */
    private String dictName;

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
