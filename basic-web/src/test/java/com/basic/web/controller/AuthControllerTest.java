package com.basic.web.controller;

import com.basic.api.dto.auth.LoginDTO;
import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.api.vo.auth.TokenVO;
import com.basic.common.result.Result;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.auth.service.IAuthService;
import com.basic.sericve.sysUser.service.ISysUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken("jwt-token");
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("plain-password");
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(authService.login("admin", "plain-password", request)).thenReturn(tokenVO);

        Result<TokenVO> result = controller.login(loginDTO, request);

        verify(authService).login("admin", "plain-password", request);
        assertThat(result.getData().getToken()).isEqualTo("jwt-token");
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

    @Test
    void getUserInfoReturnsDepartmentsFromLoginUserPrincipal() {
        ISysUserService sysUserService = mock(ISysUserService.class);
        IAuthService authService = mock(IAuthService.class);
        AuthController controller = new AuthController(sysUserService, authService);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(10L);
        loginUser.setUsername("admin");
        loginUser.setNickname("管理员");
        loginUser.setAvatar("avatar.png");
        loginUser.setDeptNames(List.of("研发部"));
        loginUser.setRoles(List.of("admin"));
        loginUser.setPermissions(List.of("system:user:list"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));

        Result<LoginVO> result = controller.getUserInfo();

        assertThat(result.getData().getUserId()).isEqualTo(10L);
        assertThat(result.getData().getUsername()).isEqualTo("admin");
        assertThat(result.getData().getDeptNames()).containsExactly("研发部");
    }
}
