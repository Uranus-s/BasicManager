package com.basic.api.vo.sysDept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 部门树VO
 *
 * @author Gas
 */
@Data
@Schema(description = "部门树节点")
public class DeptTreeVO {

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
     * 子部门列表
     */
    @Schema(description = "子部门列表")
    private List<DeptTreeVO> children;
}
