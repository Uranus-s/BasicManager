package com.basic.api.vo.sysRole;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情VO
 *
 * @author Gas
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 权限列表
     */
    private List<PermissionInfo> permissions;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 权限信息
     */
    @Data
    public static class PermissionInfo {
        private Long id;
        private String name;
        private String permission;
    }
}
