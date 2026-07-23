package com.basic.api.vo.sysPermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单权限详情VO
 *
 * @author Gas
 */
@Data
@Schema(description = "权限详细信息")
public class PermissionVO {

    /**
     * 权限ID
     */
    @Schema(description = "权限 ID", example = "1")
    private Long id;

    /**
     * 父ID
     */
    @Schema(description = "上级权限 ID", example = "0")
    private Long parentId;

    /**
     * 父权限名称
     */
    @Schema(description = "上级权限名称", example = "系统管理")
    private String parentName;

    /**
     * 名称
     */
    @Schema(description = "权限名称", example = "用户管理")
    private String name;

    /**
     * 类型 MENU/BUTTON/API
     */
    @Schema(description = "权限类型：MENU-菜单，BUTTON-按钮，API-接口", example = "MENU", allowableValues = {"MENU", "BUTTON", "API"})
    private String type;

    /**
     * 路由路径或接口路径
     */
    @Schema(description = "路由路径", example = "/system/user")
    private String path;

    /**
     * 前端组件路径
     */
    @Schema(description = "前端组件路径", example = "system/user/index")
    private String component;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:list")
    private String permission;

    /**
     * 图标
     */
    @Schema(description = "图标名称", example = "user")
    private String icon;

    /**
     * 排序
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

    /**
     * 是否显示 0=隐藏 1=显示
     */
    @Schema(description = "是否可见：0-隐藏，1-显示", example = "1", allowableValues = {"0", "1"})
    private Byte visible;

    /**
     * 状态 0=禁用 1=正常
     */
    @Schema(description = "状态：0-禁用，1-正常", example = "1", allowableValues = {"0", "1"})
    private Byte status;

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
}
