package com.basic.api.dto.sysPermission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单权限更新DTO
 *
 * @author Gas
 */
@Data
public class PermissionUpdateDTO {

    /**
     * 权限ID
     */
    @NotNull(message = "权限ID不能为空")
    private Long id;

    /**
     * 父ID
     */
    @NotNull(message = "父权限ID不能为空")
    private Long parentId;

    /**
     * 名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50")
    private String name;

    /**
     * 类型 MENU/BUTTON/API
     */
    @NotBlank(message = "权限类型不能为空")
    private String type;

    /**
     * 路由路径或接口路径
     */
    @Size(max = 200, message = "路径长度不能超过200")
    private String path;

    /**
     * 前端组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200")
    private String component;

    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100")
    private String permission;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否显示 0=隐藏 1=显示
     */
    private Byte visible;

    /**
     * 状态 0=禁用 1=正常
     */
    private Byte status;
}
