package com.basic.api.dto.sysDept;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "部门新增请求")
public class DeptAddDTO {

    /**
     * 父部门ID
     */
    @Schema(description = "父部门ID，根部门使用0", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "研发部", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50")
    private String deptName;

    /**
     * 负责人
     */
    @Schema(description = "部门负责人姓名", example = "张三")
    @Size(max = 50, message = "负责人长度不能超过50")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "负责人联系电话", example = "13800000000")
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    /**
     * 排序
     */
    @Schema(description = "显示排序，数值越小越靠前", example = "1")
    private Integer sort;
}
