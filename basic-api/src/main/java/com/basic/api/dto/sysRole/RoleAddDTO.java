package com.basic.api.dto.sysRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色新增DTO
 *
 * @author Gas
 */
@Data
public class RoleAddDTO {

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    /**
     * 权限ID列表
     */
    private java.util.List<Long> permissionIds;
}
