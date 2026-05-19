package com.basic.sericve.auth.impl;

import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.model.LoginSession;
import com.basic.core.security.service.AuthTokenService;
import com.basic.core.security.util.JwtUtil;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.sericve.auth.service.IAuthService;
import com.basic.sericve.auth.util.LoginRequestUtils;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public LoginVO login(String username, String password, HttpServletRequest request) {
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
        authTokenService.saveLoginSession(buildLoginSession(user, token, request));

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

    @Override
    public List<OnlineUserVO> getOnlineUsers() {
        return authTokenService.listOnlineSessions()
                .stream()
                .map(this::toOnlineUserVO)
                .toList();
    }

    @Override
    public void forceLogout(Long userId) {
        authTokenService.forceLogout(userId);
    }

    private LoginSession buildLoginSession(SysUser user, String token, HttpServletRequest request) {
        LoginSession session = new LoginSession();
        session.setToken(token);
        session.setUserId(user.getId());
        session.setUsername(user.getUsername());
        session.setNickname(user.getNickname());
        session.setAvatar(user.getAvatar());
        session.setLoginTime(LocalDateTime.now());
        session.setLoginIp(LoginRequestUtils.resolveMaskedIp(request));
        session.setBrowser(LoginRequestUtils.resolveBrowser(request));
        session.setOs(LoginRequestUtils.resolveOs(request));
        return session;
    }

    private OnlineUserVO toOnlineUserVO(LoginSession session) {
        OnlineUserVO onlineUserVO = new OnlineUserVO();
        onlineUserVO.setUserId(session.getUserId());
        onlineUserVO.setUsername(session.getUsername());
        onlineUserVO.setNickname(session.getNickname());
        onlineUserVO.setAvatar(session.getAvatar());
        onlineUserVO.setLoginTime(session.getLoginTime());
        onlineUserVO.setLoginIp(session.getLoginIp());
        onlineUserVO.setBrowser(session.getBrowser());
        onlineUserVO.setOs(session.getOs());
        return onlineUserVO;
    }
}
