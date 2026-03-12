package com.basic.sericve.security;

import com.basic.core.security.model.LoginUser;
import com.basic.core.security.spi.SecurityUserQueryService;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUser.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserQueryServiceImpl implements SecurityUserQueryService {

    private final ISysUserService sysUserService;
    private final ISysRoleService sysRoleService;
    private final ISysPermissionService sysPermissionService;

    @Override
    public LoginUser loadByUsername(String username) {
        // 根据用户名查询用户
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            return null;
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new RuntimeException("用户已禁用");
        }

        // 构造LoginUser对象
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setEnabled(true);

        // 获取用户角色
        java.util.List<String> roleCodes = sysRoleService.getRoleCodes(user.getId());
        loginUser.setRoles(roleCodes);

        // 获取用户权限
        java.util.List<String> permissions = sysPermissionService.getUserPermissions(user.getId());
        loginUser.setPermissions(permissions);

        return loginUser;
    }
}
