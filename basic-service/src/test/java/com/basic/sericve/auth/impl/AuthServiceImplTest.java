package com.basic.sericve.auth.impl;

import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.core.security.model.LoginSession;
import com.basic.core.security.service.AuthTokenService;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUser.service.ISysUserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @Test
    void loginCachesGeneratedSessionInRedis() {
        ISysUserService sysUserService = mock(ISysUserService.class);
        ISysRoleService sysRoleService = mock(ISysRoleService.class);
        ISysPermissionService sysPermissionService = mock(ISysPermissionService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        AuthServiceImpl service = new AuthServiceImpl(
                sysUserService,
                sysRoleService,
                sysPermissionService,
                passwordEncoder,
                authTokenService
        );

        SysUser user = new SysUser();
        user.setId(10L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setNickname("管理员");
        user.setStatus((byte) 1);

        when(sysUserService.getUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(sysRoleService.getRoleCodes(10L)).thenReturn(List.of("admin"));
        when(sysPermissionService.getUserPermissions(10L)).thenReturn(List.of("system:user:list"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.25");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36");

        LoginVO loginVO = service.login("admin", "plain-password", request);

        assertThat(loginVO.getToken()).isNotBlank();
        ArgumentCaptor<LoginSession> sessionCaptor = ArgumentCaptor.forClass(LoginSession.class);
        verify(authTokenService).saveLoginSession(sessionCaptor.capture());
        LoginSession session = sessionCaptor.getValue();
        assertThat(session.getToken()).isEqualTo(loginVO.getToken());
        assertThat(session.getUserId()).isEqualTo(10L);
        assertThat(session.getUsername()).isEqualTo("admin");
        assertThat(session.getNickname()).isEqualTo("管理员");
        assertThat(session.getLoginTime()).isNotNull();
        assertThat(session.getLoginIp()).isEqualTo("192.168.*.25");
        assertThat(session.getBrowser()).isEqualTo("Chrome");
        assertThat(session.getOs()).isEqualTo("Windows");
    }

    @Test
    void logoutDeletesTokenThroughAuthTokenService() {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        AuthServiceImpl service = new AuthServiceImpl(
                mock(ISysUserService.class),
                mock(ISysRoleService.class),
                mock(ISysPermissionService.class),
                mock(PasswordEncoder.class),
                authTokenService
        );

        service.logout("token-value");

        verify(authTokenService).deleteLoginToken("token-value");
    }

    @Test
    void getOnlineUsersMapsSessionsFromAuthTokenService() {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        AuthServiceImpl service = new AuthServiceImpl(
                mock(ISysUserService.class),
                mock(ISysRoleService.class),
                mock(ISysPermissionService.class),
                mock(PasswordEncoder.class),
                authTokenService
        );

        LocalDateTime loginTime = LocalDateTime.of(2026, 5, 12, 10, 30);
        LoginSession session = new LoginSession();
        session.setUserId(10L);
        session.setUsername("admin");
        session.setNickname("管理员");
        session.setAvatar("avatar.png");
        session.setLoginTime(loginTime);
        session.setLoginIp("192.168.*.25");
        session.setBrowser("Chrome");
        session.setOs("Windows");
        when(authTokenService.listOnlineSessions()).thenReturn(List.of(session));

        List<OnlineUserVO> onlineUsers = service.getOnlineUsers();

        assertThat(onlineUsers).hasSize(1);
        OnlineUserVO onlineUser = onlineUsers.getFirst();
        assertThat(onlineUser.getUserId()).isEqualTo(10L);
        assertThat(onlineUser.getUsername()).isEqualTo("admin");
        assertThat(onlineUser.getNickname()).isEqualTo("管理员");
        assertThat(onlineUser.getAvatar()).isEqualTo("avatar.png");
        assertThat(onlineUser.getLoginTime()).isEqualTo(loginTime);
        assertThat(onlineUser.getLoginIp()).isEqualTo("192.168.*.25");
        assertThat(onlineUser.getBrowser()).isEqualTo("Chrome");
        assertThat(onlineUser.getOs()).isEqualTo("Windows");
    }

    @Test
    void forceLogoutDelegatesToAuthTokenService() {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        AuthServiceImpl service = new AuthServiceImpl(
                mock(ISysUserService.class),
                mock(ISysRoleService.class),
                mock(ISysPermissionService.class),
                mock(PasswordEncoder.class),
                authTokenService
        );

        service.forceLogout(10L);

        verify(authTokenService).forceLogout(10L);
    }
}
