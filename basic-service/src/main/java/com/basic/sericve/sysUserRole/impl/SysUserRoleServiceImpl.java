package com.basic.sericve.sysUserRole.impl;

import com.basic.dao.sysUserRole.entity.SysUserRole;
import com.basic.dao.sysUserRole.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.sericve.sysUserRole.service.ISysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户角色关联表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除用户的所有角色
        removeAllByUserId(userId);

        // 再添加新的角色
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            saveBatch(userRoles);
        }
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = list(wrapper);
        return userRoles.stream().map(SysUserRole::getRoleId).toList();
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        List<SysUserRole> userRoles = list(wrapper);
        return userRoles.stream().map(SysUserRole::getUserId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manageRoleUsers(Long roleId, List<Long> addUserIds, List<Long> removeUserIds) {
        if (removeUserIds != null && !removeUserIds.isEmpty()) {
            LambdaQueryWrapper<SysUserRole> removeWrapper = new LambdaQueryWrapper<>();
            removeWrapper.eq(SysUserRole::getRoleId, roleId)
                    .in(SysUserRole::getUserId, removeUserIds);
            remove(removeWrapper);
        }

        if (addUserIds == null || addUserIds.isEmpty()) {
            return;
        }

        List<Long> existingUserIds = getUserIdsByRoleId(roleId);
        Set<Long> existingUserIdSet = new HashSet<>(existingUserIds);
        List<SysUserRole> userRoles = addUserIds.stream()
                .distinct()
                .filter(userId -> !existingUserIdSet.contains(userId))
                .map(userId -> {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setRoleId(roleId);
                    userRole.setUserId(userId);
                    return userRole;
                })
                .toList();
        if (!userRoles.isEmpty()) {
            saveBatch(userRoles);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        remove(wrapper);
    }
}
