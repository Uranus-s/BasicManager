package com.basic.sericve.sysUser.impl;

import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysDept.entity.SysDept;
import com.basic.dao.sysRole.entity.SysRole;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.dao.sysUser.mapper.SysUserMapper;
import com.basic.sericve.sysDept.service.ISysDeptService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUser.service.ISysUserService;
import com.basic.sericve.sysUserDept.service.ISysUserDeptService;
import com.basic.sericve.sysUserRole.service.ISysUserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final ISysUserRoleService sysUserRoleService;
    private final ISysUserDeptService sysUserDeptService;
    private final ISysDeptService sysDeptService;
    private final ISysRoleService sysRoleService;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUser(UserAddDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultEnum.USER_ALREADY_EXIST);
        }

        // 创建用户
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus((byte) 1);
        }
        save(user);

        // 分配角色
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            sysUserRoleService.assignRoles(user.getId(), dto.getRoleIds());
        }

        // 分配部门
        if (dto.getDeptIds() != null && !dto.getDeptIds().isEmpty()) {
            sysUserDeptService.assignDepts(user.getId(), dto.getDeptIds());
        }

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO dto) {
        SysUser user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        // 检查用户名是否重复
        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, dto.getUsername()).ne(SysUser::getId, dto.getId());
            if (count(wrapper) > 0) {
                throw new BusinessException(ResultEnum.USER_ALREADY_EXIST);
            }
        }

        BeanUtils.copyProperties(dto, user);
        updateById(user);

        // 更新角色
        if (dto.getRoleIds() != null) {
            sysUserRoleService.assignRoles(dto.getId(), dto.getRoleIds());
        }

        // 更新部门
        if (dto.getDeptIds() != null) {
            sysUserDeptService.assignDepts(dto.getId(), dto.getDeptIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }
        // 逻辑删除
        removeById(id);
        // 删除用户角色关联
        sysUserRoleService.removeAllByUserId(id);
        // 删除用户部门关联
        sysUserDeptService.removeAllByUserId(id);
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 通过中间表获取部门名称
        List<Long> deptIds = sysUserDeptService.getDeptIdsByUserId(id);
        if (!deptIds.isEmpty()) {
            List<SysDept> depts = sysDeptService.listByIds(deptIds);
            List<String> deptNames = depts.stream().map(SysDept::getDeptName).collect(Collectors.toList());
            vo.setDeptNames(deptNames);
        }

        // 获取角色信息
        List<Long> roleIds = sysUserRoleService.getRoleIdsByUserId(id);
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = sysRoleService.listByIds(roleIds);
            List<UserVO.RoleInfo> roleInfos = roles.stream()
                    .map(role -> {
                        UserVO.RoleInfo info = new UserVO.RoleInfo();
                        info.setId(role.getId());
                        info.setRoleCode(role.getRoleCode());
                        info.setRoleName(role.getRoleName());
                        return info;
                    })
                    .collect(Collectors.toList());
            vo.setRoles(roleInfos);
        }

        return vo;
    }

    @Override
    public PageResult<UserListVO> getUserList(UserQueryDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(SysUser::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getNickname())) {
            wrapper.like(SysUser::getNickname, dto.getNickname());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.eq(SysUser::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            wrapper.eq(SysUser::getEmail, dto.getEmail());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        List<UserListVO> voList = new ArrayList<>();
        for (SysUser user : page.getRecords()) {
            UserListVO vo = new UserListVO();
            BeanUtils.copyProperties(user, vo);

            // 通过中间表获取部门名称
            List<Long> userDeptIds = sysUserDeptService.getDeptIdsByUserId(user.getId());
            if (!userDeptIds.isEmpty()) {
                List<SysDept> depts = sysDeptService.listByIds(userDeptIds);
                List<String> deptNames = depts.stream().map(SysDept::getDeptName).collect(Collectors.toList());
                vo.setDeptNames(deptNames);
            }

            // 获取角色名称
            List<Long> roleIds = sysUserRoleService.getRoleIdsByUserId(user.getId());
            if (!roleIds.isEmpty()) {
                List<SysRole> roles = sysRoleService.listByIds(roleIds);
                List<String> roleNames = roles.stream().map(SysRole::getRoleName).collect(Collectors.toList());
                vo.setRoleNames(roleNames);
            }

            voList.add(vo);
        }

        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }
        sysUserRoleService.assignRoles(userId, roleIds);
    }

    @Override
    public List<Long> getUserRoles(Long userId) {
        return sysUserRoleService.getRoleIdsByUserId(userId);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultEnum.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }
}
