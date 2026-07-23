package com.basic.api.dto.sysDept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "部门分页查询条件")
public class DeptQueryDTO {

    /**
     * 部门名称（模糊查询）
     */
    @Schema(description = "部门名称，支持模糊匹配", example = "研发")
    private String deptName;

    /**
     * 负责人（模糊查询）
     */
    @Schema(description = "负责人姓名，支持模糊匹配", example = "张")
    private String leader;

    /**
     * 父部门ID
     */
    @Schema(description = "父部门ID", example = "1")
    private Long parentId;

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "当前页码，从1开始", example = "1", defaultValue = "1", minimum = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页记录数", example = "10", defaultValue = "10", minimum = "1")
    private Integer pageSize = 10;
}
