package com.basic.api.dto.sysRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "修改角色请求参数")
public class RoleUpdateDTO {

    /**
     * 角色ID
     */
    @Schema(description = "角色 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色ID不能为空")
    private Long id;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "系统管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "角色状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 备注
     */
    @Schema(description = "角色备注", example = "负责系统日常管理")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    /**
     * 权限ID列表
     */
    @Schema(description = "角色拥有的权限 ID 列表", example = "[1, 2, 3]")
    private java.util.List<Long> permissionIds;
}
