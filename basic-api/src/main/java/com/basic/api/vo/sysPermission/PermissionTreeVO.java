package com.basic.api.vo.sysPermission;

import lombok.Data;

import java.util.List;

/**
 * 菜单权限树VO
 *
 * @author Gas
 */
@Data
public class PermissionTreeVO {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 父ID
     */
    private Long parentId;

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
     * 子权限列表
     */
    private List<PermissionTreeVO> children;
}
