package com.basic.api.vo.sysDept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门详情VO
 *
 * @author Gas
 */
@Data
@Schema(description = "部门详细信息")
public class DeptVO {

    /**
     * 部门ID
     */
    @Schema(description = "部门 ID", example = "1")
    private Long id;

    /**
     * 父部门ID
     */
    @Schema(description = "上级部门 ID，0 表示根部门", example = "0")
    private Long parentId;

    /**
     * 父部门名称
     */
    @Schema(description = "上级部门名称", example = "总公司")
    private String parentName;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    /**
     * 负责人
     */
    @Schema(description = "部门负责人", example = "张三")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "13800000000")
    private String phone;

    /**
     * 排序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-23T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-07-23T10:30:00")
    private LocalDateTime updateTime;
}
