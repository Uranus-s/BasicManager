package com.basic.api.dto.sysRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色查询DTO
 *
 * @author Gas
 */
@Data
@Schema(description = "角色分页查询参数")
public class RoleQueryDTO {

    /**
     * 角色编码（模糊查询）
     */
    @Schema(description = "角色编码，支持模糊查询", example = "admin")
    private String roleCode;

    /**
     * 角色名称（模糊查询）
     */
    @Schema(description = "角色名称，支持模糊查询", example = "管理员")
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "角色状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1", minimum = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页条数", example = "10", defaultValue = "10", minimum = "1")
    private Integer pageSize = 10;
}
