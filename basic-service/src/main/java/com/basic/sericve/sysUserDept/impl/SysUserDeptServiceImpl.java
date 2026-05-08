package com.basic.sericve.sysUserDept.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.basic.dao.sysUserDept.entity.SysUserDept;
import com.basic.dao.sysUserDept.mapper.SysUserDeptMapper;
import com.basic.sericve.sysUserDept.service.ISysUserDeptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户部门关联表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
public class SysUserDeptServiceImpl extends ServiceImpl<SysUserDeptMapper, SysUserDept> implements ISysUserDeptService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignDepts(Long userId, List<Long> deptIds) {
        // 先删除用户的所有部门
        removeAllByUserId(userId);

        // 再添加新的部门
        if (deptIds != null && !deptIds.isEmpty()) {
            List<SysUserDept> userDepts = new ArrayList<>();
            for (Long deptId : deptIds) {
                SysUserDept userDept = new SysUserDept();
                userDept.setUserId(userId);
                userDept.setDeptId(deptId);
                userDepts.add(userDept);
            }
            saveBatch(userDepts);
        }
    }

    @Override
    public List<Long> getDeptIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDept::getUserId, userId);
        List<SysUserDept> userDepts = list(wrapper);
        return userDepts.stream().map(SysUserDept::getDeptId).toList();
    }

    @Override
    public List<Long> getUserIdsByDeptId(Long deptId) {
        LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDept::getDeptId, deptId);
        List<SysUserDept> userDepts = list(wrapper);
        return userDepts.stream().map(SysUserDept::getUserId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUsersToDept(Long deptId, List<Long> userIds) {
        List<Long> distinctUserIds = userIds.stream().distinct().toList();
        List<Long> existingUserIds = getUserIdsByDeptId(deptId);
        Set<Long> existingUserIdSet = existingUserIds.stream().collect(Collectors.toSet());

        List<SysUserDept> userDepts = distinctUserIds.stream()
                .filter(userId -> !existingUserIdSet.contains(userId))
                .map(userId -> {
                    SysUserDept userDept = new SysUserDept();
                    userDept.setDeptId(deptId);
                    userDept.setUserId(userId);
                    return userDept;
                })
                .toList();

        if (!userDepts.isEmpty()) {
            saveBatch(userDepts);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUsersFromDept(Long deptId, List<Long> userIds) {
        LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDept::getDeptId, deptId)
                .in(SysUserDept::getUserId, userIds);
        remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllByUserId(Long userId) {
        LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserDept::getUserId, userId);
        remove(wrapper);
    }
}
