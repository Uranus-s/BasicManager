package com.basic.sericve.sysUser.impl;

import com.basic.api.dto.sysUser.UserAddDTO;
import com.basic.api.dto.sysUser.UserQueryDTO;
import com.basic.api.dto.sysUser.UserUpdateDTO;
import com.basic.api.vo.auth.InitResultVO;
import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.api.vo.sysUser.UserListVO;
import com.basic.api.vo.sysUser.UserVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.PageResult;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.model.LoginUser;
import com.basic.core.security.util.JwtUtil;
import com.basic.dao.sysDept.entity.SysDept;
import com.basic.dao.sysPermission.entity.SysPermission;
import com.basic.dao.sysRole.entity.SysRole;
import com.basic.dao.sysRolePermission.entity.SysRolePermission;
import com.basic.dao.sysRolePermission.mapper.SysRolePermissionMapper;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.dao.sysUserRole.entity.SysUserRole;
import com.basic.dao.sysUserRole.mapper.SysUserRoleMapper;
import com.basic.dao.sysUser.mapper.SysUserMapper;
import com.basic.sericve.sysDept.service.ISysDeptService;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final ISysPermissionService sysPermissionService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
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

    @Override
    public LoginVO login(String username, String password) {
        // 根据用户名查询用户
        SysUser user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ResultEnum.ACCOUNT_DISABLED);
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultEnum.PASSWORD_ERROR);
        }

        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());

        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());

        // 获取用户角色
        List<String> roleCodes = sysRoleService.getRoleCodes(user.getId());
        loginVO.setRoles(roleCodes);

        // 获取用户权限
        List<String> permissions = getUserPermissions(user.getId());
        loginVO.setPermissions(permissions);

        return loginVO;
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 使用ISysPermissionService获取用户权限
        return sysPermissionService.getUserPermissions(userId);
    }

    @Override
    public List<PermissionTreeVO> getUserRoutes() {
        // 1. 获取当前登录用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUserId();

        // 2. 获取用户所有权限实体列表
        List<SysPermission> permissions = sysPermissionService.getPermissionsByUserId(userId);

        // 3. 过滤可用权限（status=1 且 visible=1）
        List<SysPermission> activePermissions = permissions.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .filter(p -> p.getVisible() == null || p.getVisible() == 1)
                .collect(Collectors.toList());

        // 4. 构建树形结构
        return buildPermissionTree(activePermissions);
    }

    /**
     * 构建权限树形结构
     */
    private List<PermissionTreeVO> buildPermissionTree(List<SysPermission> permissions) {
        // 按parentId分组
        Map<Long, List<SysPermission>> parentMap = permissions.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() != null ? p.getParentId() : 0L));

        // 获取所有顶级菜单（parentId = 0）
        List<SysPermission> rootPermissions = parentMap.getOrDefault(0L, Collections.emptyList());

        // 构建树
        return rootPermissions.stream()
                .map(p -> convertToPermissionTreeVO(p, parentMap))
                .sorted(Comparator.comparingInt(PermissionTreeVO::getSort))
                .collect(Collectors.toList());
    }

    /**
     * 递归转换权限为PermissionTreeVO
     */
    private PermissionTreeVO convertToPermissionTreeVO(SysPermission permission, Map<Long, List<SysPermission>> parentMap) {
        PermissionTreeVO vo = new PermissionTreeVO();
        vo.setId(permission.getId());
        vo.setParentId(permission.getParentId());
        vo.setName(permission.getName());
        vo.setType(permission.getType());
        vo.setPath(permission.getPath());
        vo.setComponent(permission.getComponent());
        vo.setPermission(permission.getPermission());
        vo.setIcon(permission.getIcon());
        vo.setSort(permission.getSort());
        vo.setVisible(permission.getVisible());
        vo.setStatus(permission.getStatus());

        // 递归处理子权限
        List<SysPermission> children = parentMap.getOrDefault(permission.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            vo.setChildren(children.stream()
                    .map(c -> convertToPermissionTreeVO(c, parentMap))
                    .sorted(Comparator.comparingInt(PermissionTreeVO::getSort))
                    .collect(Collectors.toList()));
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InitResultVO initAdmin(String adminPassword) {
        // 1. 检查是否已存在用户
        if (count() > 0) {
            throw new BusinessException(ResultEnum.DATA_ALREADY_EXIST);
        }

        // 2. 创建管理员角色
        SysRole adminRole = new SysRole();
        adminRole.setRoleCode("admin");
        adminRole.setRoleName("管理员");
        adminRole.setStatus((byte) 1);
        adminRole.setRemark("系统超级管理员");
        sysRoleService.save(adminRole);

        // 3. 创建管理员用户
        SysUser adminUser = new SysUser();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setNickname("管理员");
        adminUser.setStatus((byte) 1);
        save(adminUser);

        // 4. 关联用户角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(adminUser.getId());
        userRole.setRoleId(adminRole.getId());
        sysUserRoleMapper.insert(userRole);

        // 5. 创建权限菜单并关联角色
        List<Long> permissionIds = createInitPermissions();
        if (!permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                SysRolePermission rolePerm = new SysRolePermission();
                rolePerm.setRoleId(adminRole.getId());
                rolePerm.setPermissionId(permId);
                sysRolePermissionMapper.insert(rolePerm);
            }
        }

        // 6. 返回结果
        InitResultVO result = new InitResultVO();
        result.setUserId(adminUser.getId());
        result.setUsername(adminUser.getUsername());
        result.setRoleId(adminRole.getId());
        result.setRoleCode(adminRole.getRoleCode());
        return result;
    }

    /**
     * 创建初始化权限菜单
     *
     * @return 权限ID列表
     */
    private List<Long> createInitPermissions() {
        List<Long> permissionIds = new ArrayList<>();

        // 系统管理菜单
        SysPermission systemMenu = new SysPermission();
        systemMenu.setParentId(0L);
        systemMenu.setName("系统管理");
        systemMenu.setType("MENU");
        systemMenu.setPath("/system");
        systemMenu.setIcon("Setting");
        systemMenu.setSort(1);
        systemMenu.setVisible((byte) 1);
        systemMenu.setStatus((byte) 1);
        sysPermissionService.save(systemMenu);
        permissionIds.add(systemMenu.getId());

        // 子菜单定义: 名称, 路径, 权限标识, 图标, 排序
        String[][] childMenus = {
            {"用户管理", "/system/user", "system:user:list", "User", "1"},
            {"角色管理", "/system/role", "system:role:list", "peoples", "2"},
            {"菜单管理", "/system/permission", "system:permission:list", "menu", "3"},
            {"部门管理", "/system/dept", "system:dept:list", "office", "4"},
            {"字典管理", "/system/dict", "system:dict:list", "dict", "5"},
            {"参数配置", "/system/config", "system:config:list", "config", "6"},
        };

        // 日志管理菜单
        SysPermission logMenu = new SysPermission();
        logMenu.setParentId(systemMenu.getId());
        logMenu.setName("日志管理");
        logMenu.setType("MENU");
        logMenu.setPath("/system/log");
        logMenu.setIcon("Log");
        logMenu.setSort(7);
        logMenu.setVisible((byte) 1);
        logMenu.setStatus((byte) 1);
        sysPermissionService.save(logMenu);
        permissionIds.add(logMenu.getId());

        // 日志子菜单
        String[][] logChildMenus = {
            {"登录日志", "/system/loginLog", "system:loginLog:list", "login", "1"},
            {"操作日志", "/system/operLog", "system:operLog:list", "operation", "2"},
        };

        // 保存子菜单
        int sort = 1;
        for (String[] menu : childMenus) {
            SysPermission perm = new SysPermission();
            perm.setParentId(systemMenu.getId());
            perm.setName(menu[0]);
            perm.setType("MENU");
            perm.setPath(menu[1]);
            perm.setPermission(menu[2]);
            perm.setIcon(menu[3]);
            perm.setSort(sort++);
            perm.setVisible((byte) 1);
            perm.setStatus((byte) 1);
            sysPermissionService.save(perm);
            permissionIds.add(perm.getId());
        }

        // 保存日志子菜单
        sort = 1;
        for (String[] menu : logChildMenus) {
            SysPermission perm = new SysPermission();
            perm.setParentId(logMenu.getId());
            perm.setName(menu[0]);
            perm.setType("MENU");
            perm.setPath(menu[1]);
            perm.setPermission(menu[2]);
            perm.setIcon(menu[3]);
            perm.setSort(sort++);
            perm.setVisible((byte) 1);
            perm.setStatus((byte) 1);
            sysPermissionService.save(perm);
            permissionIds.add(perm.getId());
        }

        return permissionIds;
    }
}
