package com.basic.web.controller;

import com.basic.api.dto.auth.LoginDTO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.common.result.Result;
import com.basic.sericve.auth.service.IAuthService;
import com.basic.sericve.sysUser.service.ISysUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void logoutDelegatesTokenInvalidationToUserService() {
        ISysUserService sysUserService = mock(ISysUserService.class);
        IAuthService authService = mock(IAuthService.class);
        AuthController controller = new AuthController(sysUserService, authService);

        controller.logout("Bearer token-value");

        verify(authService).logout("token-value");
    }

    @Test
    void loginDelegatesRequestToAuthService() {
        ISysUserService sysUserService = mock(ISysUserService.class);
        IAuthService authService = mock(IAuthService.class);
        AuthController controller = new AuthController(sysUserService, authService);
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("plain-password");
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.login(loginDTO, request);

        verify(authService).login("admin", "plain-password", request);
    }

    @Test
    void getOnlineUsersReturnsAuthServiceResult() {
        ISysUserService sysUserService = mock(ISysUserService.class);
        IAuthService authService = mock(IAuthService.class);
        AuthController controller = new AuthController(sysUserService, authService);
        OnlineUserVO onlineUserVO = new OnlineUserVO();
        onlineUserVO.setUserId(10L);
        when(authService.getOnlineUsers()).thenReturn(List.of(onlineUserVO));

        Result<List<OnlineUserVO>> result = controller.getOnlineUsers();

        assertThat(result.getData()).containsExactly(onlineUserVO);
    }

    @Test
    void forceLogoutDelegatesToAuthService() {
        ISysUserService sysUserService = mock(ISysUserService.class);
        IAuthService authService = mock(IAuthService.class);
        AuthController controller = new AuthController(sysUserService, authService);

        controller.forceLogout(10L);

        verify(authService).forceLogout(10L);
    }
}
