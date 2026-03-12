package com.basic.api.dto.sysRole;

import lombok.Data;

/**
 * 角色查询DTO
 *
 * @author Gas
 */
@Data
public class RoleQueryDTO {

    /**
     * 角色编码（模糊查询）
     */
    private String roleCode;

    /**
     * 角色名称（模糊查询）
     */
    private String roleName;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
