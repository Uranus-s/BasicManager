package com.basic.api.vo.sysPermission;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单权限详情VO
 *
 * @author Gas
 */
@Data
public class PermissionVO {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 父ID
     */
    private Long parentId;

    /**
     * 父权限名称
     */
    private String parentName;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型 MENU/BUTTON/API
     */
    private String type;

    /**
     * 路由路径或接口路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 权限标识
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
