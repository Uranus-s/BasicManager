package com.basic.api.dto.sysDept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门新增DTO
 *
 * @author Gas
 */
@Data
public class DeptAddDTO {

    /**
     * 父部门ID
     */
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50")
    private String deptName;

    /**
     * 负责人
     */
    @Size(max = 50, message = "负责人长度不能超过50")
    private String leader;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    /**
     * 排序
     */
    private Integer sort;
}
