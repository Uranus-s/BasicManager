package com.basic.api.vo.sysRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情VO
 *
 * @author Gas
 */
@Data
@Schema(description = "角色详细信息")
public class RoleVO {

    /**
     * 角色ID
     */
    @Schema(description = "角色 ID", example = "1")
    private Long id;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", example = "system_admin")
    private String roleCode;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "系统管理员")
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

    /**
     * 备注
     */
    @Schema(description = "角色备注", example = "负责系统日常管理")
    private String remark;

    /**
     * 权限列表
     */
    @Schema(description = "角色拥有的权限列表")
    private List<PermissionInfo> permissions;

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

    /**
     * 权限信息
     */
    @Data
    @Schema(description = "角色权限摘要")
    public static class PermissionInfo {
        @Schema(description = "权限 ID", example = "1")
        private Long id;
        @Schema(description = "权限名称", example = "用户查询")
        private String name;
        @Schema(description = "权限标识", example = "system:user:list")
        private String permission;
    }
}
