package com.basic.api.dto.sysDept;

import lombok.Data;

/**
 * 部门查询DTO
 *
 * @author Gas
 */
@Data
public class DeptQueryDTO {

    /**
     * 部门名称（模糊查询）
     */
    private String deptName;

    /**
     * 负责人（模糊查询）
     */
    private String leader;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
