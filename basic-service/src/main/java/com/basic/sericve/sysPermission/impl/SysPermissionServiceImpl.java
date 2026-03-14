package com.basic.sericve.sysPermission.impl;

import com.basic.api.dto.sysPermission.PermissionAddDTO;
import com.basic.api.dto.sysPermission.PermissionQueryDTO;
import com.basic.api.dto.sysPermission.PermissionUpdateDTO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysPermission.PermissionVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysPermission.entity.SysPermission;
import com.basic.dao.sysPermission.mapper.SysPermissionMapper;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRolePermission.service.ISysRolePermissionService;
import com.basic.sericve.sysUserRole.service.ISysUserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final ISysRolePermissionService sysRolePermissionService;
    private final ISysUserRoleService sysUserRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPermission(PermissionAddDTO dto) {
        // 创建权限
        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(dto, permission);
        if (permission.getSort() == null) {
            permission.setSort(0);
        }
        if (permission.getVisible() == null) {
            permission.setVisible((byte) 1);
        }
        if (permission.getStatus() == null) {
            permission.setStatus((byte) 1);
        }
        save(permission);
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(PermissionUpdateDTO dto) {
        SysPermission permission = getById(dto.getId());
        if (permission == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        // 检查是否将权限设置为自己或自己的子权限
        if (dto.getId().equals(dto.getParentId())) {
            throw new BusinessException(ResultEnum.PARAM_ILLEGAL);
        }

        BeanUtils.copyProperties(dto, permission);
        updateById(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        // 检查是否有子权限
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, id);
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultEnum.STATUS_NOT_ALLOWED);
        }

        SysPermission permission = getById(id);
        if (permission == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
    }

    @Override
    public PermissionVO getPermissionById(Long id) {
        SysPermission permission = getById(id);
        if (permission == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);

        // 获取父权限名称
        if (permission.getParentId() != null && !permission.getParentId().equals(0L)) {
            SysPermission parentPermission = getById(permission.getParentId());
            if (parentPermission != null) {
                vo.setParentName(parentPermission.getName());
            }
        }

        return vo;
    }

    @Override
    public PageResult<PermissionVO> getPermissionList(PermissionQueryDTO dto) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(SysPermission::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(SysPermission::getType, dto.getType());
        }
        if (dto.getParentId() != null) {
            wrapper.eq(SysPermission::getParentId, dto.getParentId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysPermission::getStatus, dto.getStatus());
        }
        wrapper.orderByAsc(SysPermission::getSort).orderByDesc(SysPermission::getCreateTime);

        IPage<SysPermission> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<PermissionVO> voList = new ArrayList<>();
        for (SysPermission permission : page.getRecords()) {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);

            // 获取父权限名称
            if (permission.getParentId() != null && !permission.getParentId().equals(0L)) {
                SysPermission parentPermission = getById(permission.getParentId());
                if (parentPermission != null) {
                    vo.setParentName(parentPermission.getName());
                }
            }

            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        // 查询所有权限
        List<SysPermission> allPermissions = list(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort));

        // 构建树形结构
        return buildPermissionTree(0L, allPermissions);
    }

    private List<PermissionTreeVO> buildPermissionTree(Long parentId, List<SysPermission> allPermissions) {
        List<PermissionTreeVO> tree = new ArrayList<>();
        for (SysPermission permission : allPermissions) {
            if (permission.getParentId().equals(parentId)) {
                PermissionTreeVO vo = new PermissionTreeVO();
                BeanUtils.copyProperties(permission, vo);
                vo.setChildren(buildPermissionTree(permission.getId(), allPermissions));
                tree.add(vo);
            }
        }
        return tree;
    }

    @Override
    public List<PermissionVO> getAllPermissions() {
        List<SysPermission> permissions = list(new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort));

        return permissions.stream()
                .map(permission -> {
                    PermissionVO vo = new PermissionVO();
                    BeanUtils.copyProperties(permission, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 获取用户角色
        List<Long> roleIds = sysUserRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取角色权限
        List<Long> permissionIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<Long> perms = sysRolePermissionService.getPermissionIdsByRoleId(roleId);
            permissionIds.addAll(perms);
        }

        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取权限标识
        List<SysPermission> permissions = listByIds(permissionIds);
        return permissions.stream()
                .map(SysPermission::getPermission)
                .filter(p -> StringUtils.hasText(p))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<SysPermission> getPermissionsByUserId(Long userId) {
        // 获取用户角色
        List<Long> roleIds = sysUserRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取角色权限ID
        Set<Long> permissionIds = new HashSet<>();
        for (Long roleId : roleIds) {
            List<Long> perms = sysRolePermissionService.getPermissionIdsByRoleId(roleId);
            permissionIds.addAll(perms);
        }

        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取权限实体列表
        return listByIds(new ArrayList<>(permissionIds));
    }
}
