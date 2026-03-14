package com.basic.sericve.sysRolePermission.impl;

import com.basic.dao.sysRolePermission.entity.SysRolePermission;
import com.basic.dao.sysRolePermission.mapper.SysRolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.sericve.sysRolePermission.service.ISysRolePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除角色的所有权限
        removeAllByRoleId(roleId);

        // 再添加新的权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            saveBatch(rolePermissions);
        }
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        List<SysRolePermission> rolePermissions = list(wrapper);
        return rolePermissions.stream().map(SysRolePermission::getPermissionId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        remove(wrapper);
    }
}
