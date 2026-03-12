package com.basic.api.dto.sysPermission;

import lombok.Data;

/**
 * 菜单权限查询DTO
 *
 * @author Gas
 */
@Data
public class PermissionQueryDTO {

    /**
     * 名称（模糊查询）
     */
    private String name;

    /**
     * 类型 MENU/BUTTON/API
     */
    private String type;

    /**
     * 父ID
     */
    private Long parentId;

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
