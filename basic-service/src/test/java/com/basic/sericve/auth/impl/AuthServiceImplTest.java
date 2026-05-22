package com.basic.sericve.auth.impl;

import com.basic.api.dto.auth.ForgotPasswordResetDTO;
import com.basic.api.dto.auth.RegisterDTO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.core.security.service.AuthTokenService;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUser.service.ISysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private ISysUserService sysUserService;

    @Mock
    private ISysRoleService sysRoleService;

    @Mock
    private ISysPermissionService sysPermissionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                sysUserService,
                sysRoleService,
                sysPermissionService,
                passwordEncoder,
                authTokenService);
    }

    @Test
    void registerShouldCreateEnabledUserWithEncodedPassword() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setNickname("新用户");
        dto.setPhone("13800138000");
        dto.setEmail("newuser@example.com");
        dto.setAvatar("avatar.png");

        when(sysUserService.getUserByUsername("newuser")).thenReturn(null);
        when(sysUserService.count(any())).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(sysUserService.save(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(100L);
            return true;
        });

        Long userId = authService.register(dto);

        assertEquals(100L, userId);
        ArgumentCaptor<SysUser> captor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserService).save(captor.capture());
        SysUser savedUser = captor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("新用户", savedUser.getNickname());
        assertEquals("13800138000", savedUser.getPhone());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertEquals("avatar.png", savedUser.getAvatar());
        assertEquals((byte) 1, savedUser.getStatus());
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existing");
        dto.setPassword("123456");

        SysUser existingUser = new SysUser();
        existingUser.setId(1L);
        existingUser.setUsername("existing");
        when(sysUserService.getUserByUsername("existing")).thenReturn(existingUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(dto));

        assertEquals(ResultEnum.USER_ALREADY_EXIST.getCode(), exception.getError().getCode());
        verify(sysUserService, never()).save(any(SysUser.class));
    }

    @Test
    void registerShouldRejectDuplicatePhone() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setPhone("13800138000");

        when(sysUserService.getUserByUsername("newuser")).thenReturn(null);
        when(sysUserService.count(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(dto));

        assertEquals(ResultEnum.PHONE_ALREADY_BIND.getCode(), exception.getError().getCode());
        verify(sysUserService, never()).save(any(SysUser.class));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setPhone("13800138000");
        dto.setEmail("newuser@example.com");

        when(sysUserService.getUserByUsername("newuser")).thenReturn(null);
        when(sysUserService.count(any())).thenReturn(0L, 1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(dto));

        assertEquals(ResultEnum.EMAIL_ALREADY_BIND.getCode(), exception.getError().getCode());
        verify(sysUserService, never()).save(any(SysUser.class));
    }

    @Test
    void resetForgottenPasswordShouldRejectMismatchedContact() {
        ForgotPasswordResetDTO dto = new ForgotPasswordResetDTO();
        dto.setUsername("newuser");
        dto.setContact("wrong@example.com");
        dto.setNewPassword("654321");

        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("newuser");
        user.setPhone("13800138000");
        user.setEmail("newuser@example.com");
        when(sysUserService.getUserByUsername("newuser")).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.resetForgottenPassword(dto));

        assertEquals(ResultEnum.PARAM_ILLEGAL.getCode(), exception.getError().getCode());
        verify(passwordEncoder, never()).encode(any());
        verify(sysUserService, never()).updateById(any(SysUser.class));
        verify(authTokenService, never()).forceLogout(any());
    }

    @Test
    void resetForgottenPasswordShouldUpdatePasswordAndForceLogout() {
        ForgotPasswordResetDTO dto = new ForgotPasswordResetDTO();
        dto.setUsername("newuser");
        dto.setContact("newuser@example.com");
        dto.setNewPassword("654321");

        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("newuser");
        user.setPhone("13800138000");
        user.setEmail("newuser@example.com");
        user.setPassword("old-password");
        when(sysUserService.getUserByUsername("newuser")).thenReturn(user);
        when(passwordEncoder.encode("654321")).thenReturn("encoded-new-password");

        authService.resetForgottenPassword(dto);

        assertEquals("encoded-new-password", user.getPassword());
        verify(sysUserService).updateById(user);
        verify(authTokenService).forceLogout(100L);
    }
}
