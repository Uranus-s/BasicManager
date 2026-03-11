package com.basic.sericve.sysRolePermission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.basic.dao.sysRolePermission.entity.SysRolePermission;

import java.util.List;

/**
 * <p>
 * 角色权限关联表 服务类
 * </p>
 *
 * @author Gas
 */
public interface ISysRolePermissionService extends IService<SysRolePermission> {

    /**
     * 分配权限
     *
     * @param roleId       角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /**
     * 移除角色所有权限
     *
     * @param roleId 角色ID
     */
    void removeAllByRoleId(Long roleId);
}
