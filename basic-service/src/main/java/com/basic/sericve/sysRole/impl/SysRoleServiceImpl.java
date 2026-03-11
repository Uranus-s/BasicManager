package com.basic.sericve.sysRole.impl;

import com.basic.api.dto.sysRole.RoleAddDTO;
import com.basic.api.dto.sysRole.RoleQueryDTO;
import com.basic.api.dto.sysRole.RoleUpdateDTO;
import com.basic.api.vo.sysRole.RoleListVO;
import com.basic.api.vo.sysRole.RoleVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysPermission.entity.SysPermission;
import com.basic.dao.sysRole.entity.SysRole;
import com.basic.dao.sysRole.mapper.SysRoleMapper;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysRolePermission.service.ISysRolePermissionService;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统角色表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final ISysRolePermissionService sysRolePermissionService;
    private final ISysPermissionService sysPermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addRole(RoleAddDTO dto) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultEnum.DATA_ALREADY_EXIST);
        }

        // 创建角色
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        if (role.getStatus() == null) {
            role.setStatus((byte) 1);
        }
        save(role);

        // 分配权限
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            sysRolePermissionService.assignPermissions(role.getId(), dto.getPermissionIds());
        }

        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateDTO dto) {
        SysRole role = getById(dto.getId());
        if (role == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        BeanUtils.copyProperties(dto, role);
        updateById(role);

        // 更新权限
        if (dto.getPermissionIds() != null) {
            sysRolePermissionService.assignPermissions(dto.getId(), dto.getPermissionIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
        // 删除角色权限关联
        sysRolePermissionService.removeAllByRoleId(id);
    }

    @Override
    public RoleVO getRoleById(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }

        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 获取权限信息
        List<Long> permissionIds = sysRolePermissionService.getPermissionIdsByRoleId(id);
        if (!permissionIds.isEmpty()) {
            List<SysPermission> permissions = sysPermissionService.listByIds(permissionIds);
            List<RoleVO.PermissionInfo> permissionInfos = permissions.stream()
                    .map(permission -> {
                        RoleVO.PermissionInfo info = new RoleVO.PermissionInfo();
                        info.setId(permission.getId());
                        info.setName(permission.getName());
                        info.setPermission(permission.getPermission());
                        return info;
                    })
                    .collect(Collectors.toList());
            vo.setPermissions(permissionInfos);
        }

        return vo;
    }

    @Override
    public PageResult<RoleListVO> getRoleList(RoleQueryDTO dto) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getRoleCode())) {
            wrapper.like(SysRole::getRoleCode, dto.getRoleCode());
        }
        if (StringUtils.hasText(dto.getRoleName())) {
            wrapper.like(SysRole::getRoleName, dto.getRoleName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysRole::getCreateTime);

        IPage<SysRole> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<RoleListVO> voList = new ArrayList<>();
        for (SysRole role : page.getRecords()) {
            RoleListVO vo = new RoleListVO();
            BeanUtils.copyProperties(role, vo);
            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    public List<RoleListVO> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, (byte) 1);
        wrapper.orderByAsc(SysRole::getId);
        List<SysRole> roles = list(wrapper);

        return roles.stream()
                .map(role -> {
                    RoleListVO vo = new RoleListVO();
                    BeanUtils.copyProperties(role, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw new BusinessException(ResultEnum.DATA_NOT_EXIST);
        }
        sysRolePermissionService.assignPermissions(roleId, permissionIds);
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return sysRolePermissionService.getPermissionIdsByRoleId(roleId);
    }
}
