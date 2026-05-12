package com.basic.sericve.auth.impl;

import com.basic.api.vo.auth.LoginVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.service.AuthTokenService;
import com.basic.core.security.util.JwtUtil;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.sericve.auth.service.IAuthService;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUser.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final ISysUserService sysUserService;
    private final ISysRoleService sysRoleService;
    private final ISysPermissionService sysPermissionService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Override
    public LoginVO login(String username, String password) {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultEnum.USER_NOT_EXIST);
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ResultEnum.ACCOUNT_DISABLED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultEnum.PASSWORD_ERROR);
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        authTokenService.saveLoginToken(user.getId(), token);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());

        List<String> roleCodes = sysRoleService.getRoleCodes(user.getId());
        loginVO.setRoles(roleCodes);

        List<String> permissions = sysPermissionService.getUserPermissions(user.getId());
        loginVO.setPermissions(permissions);

        return loginVO;
    }

    @Override
    public void logout(String token) {
        authTokenService.deleteLoginToken(token);
    }
}
