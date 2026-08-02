package com.basic.sericve.sysUser.impl;

import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.exception.BusinessException;
import com.basic.common.result.ResultEnum;
import com.basic.dao.sysRolePermission.mapper.SysRolePermissionMapper;
import com.basic.dao.sysPermission.entity.SysPermission;
import com.basic.dao.sysRole.entity.SysRole;
import com.basic.dao.sysUser.entity.SysUser;
import com.basic.dao.sysUserRole.mapper.SysUserRoleMapper;
import com.basic.sericve.sysDept.service.ISysDeptService;
import com.basic.sericve.sysFile.service.ISysFileService;
import com.basic.sericve.sysPermission.service.ISysPermissionService;
import com.basic.sericve.sysRole.service.ISysRoleService;
import com.basic.sericve.sysUserDept.service.ISysUserDeptService;
import com.basic.sericve.sysUserRole.service.ISysUserRoleService;
import com.basic.core.security.service.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private ISysUserRoleService sysUserRoleService;

    @Mock
    private ISysUserDeptService sysUserDeptService;

    @Mock
    private ISysDeptService sysDeptService;

    @Mock
    private ISysRoleService sysRoleService;

    @Mock
    private ISysPermissionService sysPermissionService;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ISysFileService sysFileService;

    @Mock
    private AuthTokenService authTokenService;

    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        sysUserService = spy(new SysUserServiceImpl(
                sysUserRoleService,
                sysUserDeptService,
                sysDeptService,
                sysRoleService,
                sysPermissionService,
                sysUserRoleMapper,
                sysRolePermissionMapper,
                passwordEncoder,
                sysFileService,
                authTokenService
        ));
    }

    @Test
    void updateCurrentUserAvatarUploadsFileAndPersistsAvatarPath() {
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar".getBytes(StandardCharsets.UTF_8)
        );
        SysUser user = new SysUser();
        user.setId(100L);
        FileVO fileVO = new FileVO();
        fileVO.setFilePath("/uploads/avatar/20260527/a.png");

        when(sysFileService.uploadFile(avatar, "avatar")).thenReturn(fileVO);
        doReturn(user).when(sysUserService).getById(100L);
        doReturn(true).when(sysUserService).updateById(any(SysUser.class));

        String avatarPath = sysUserService.updateCurrentUserAvatar(100L, avatar);

        assertEquals("/uploads/avatar/20260527/a.png", avatarPath);
        assertEquals("/uploads/avatar/20260527/a.png", user.getAvatar());
        verify(sysFileService).uploadFile(avatar, "avatar");
        verify(sysUserService).updateById(user);
    }

    @Test
    void updateCurrentUserAvatarThrowsWhenUserDoesNotExist() {
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar".getBytes(StandardCharsets.UTF_8)
        );
        doReturn(null).when(sysUserService).getById(100L);

        assertThrows(BusinessException.class, () -> sysUserService.updateCurrentUserAvatar(100L, avatar));
        verifyNoInteractions(sysFileService);
    }

    @Test
    void getCurrentUserAvatarReturnsStoredAvatarPath() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setAvatar("/uploads/avatar/20260527/a.png");
        doReturn(user).when(sysUserService).getById(100L);

        assertEquals("/uploads/avatar/20260527/a.png", sysUserService.getCurrentUserAvatar(100L));
    }

    @Test
    void resetPasswordShouldEncodeTemporaryPasswordAndForceLogout() {
        SysUser user = new SysUser();
        user.setId(100L);
        doReturn(user).when(sysUserService).getById(100L);
        when(passwordEncoder.encode("Temp123!")).thenReturn("encoded-temp");
        doReturn(true).when(sysUserService).updateById(user);

        sysUserService.resetPassword(100L, "Temp123!");

        assertEquals("encoded-temp", user.getPassword());
        verify(sysUserService).updateById(user);
        verify(authTokenService).forceLogout(100L);
    }

    @Test
    void resetPasswordShouldNotForceLogoutWhenDataVersionExpired() {
        SysUser user = new SysUser();
        user.setId(100L);
        doReturn(user).when(sysUserService).getById(100L);
        when(passwordEncoder.encode("Temp123!")).thenReturn("encoded-temp");
        doReturn(false).when(sysUserService).updateById(user);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> sysUserService.resetPassword(100L, "Temp123!"));

        assertEquals(ResultEnum.DATA_VERSION_EXPIRED, exception.getError());
        verify(authTokenService, never()).forceLogout(100L);
    }

    @Test
    void initAdminShouldCreateTypedSystemSettingsPermissions() {
        List<SysPermission> savedPermissions = new ArrayList<>();
        AtomicLong permissionId = new AtomicLong(100L);
        doReturn(0L).when(sysUserService).count();
        when(sysRoleService.save(any(SysRole.class))).thenAnswer(invocation -> {
            invocation.<SysRole>getArgument(0).setId(1L);
            return true;
        });
        doReturn(true).when(sysUserService).save(any(SysUser.class));
        when(sysPermissionService.save(any(SysPermission.class))).thenAnswer(invocation -> {
            SysPermission permission = invocation.getArgument(0);
            permission.setId(permissionId.getAndIncrement());
            savedPermissions.add(permission);
            return true;
        });

        sysUserService.initAdmin("Admin123!");

        SysPermission settingsMenu = savedPermissions.stream()
                .filter(permission -> "system:config:query".equals(permission.getPermission()))
                .findFirst()
                .orElseThrow();
        assertEquals("系统设置", settingsMenu.getName());
        assertEquals("system/config/index.vue", settingsMenu.getComponent());
        assertTrue(savedPermissions.stream().anyMatch(permission ->
                "system:config:edit".equals(permission.getPermission())
                        && settingsMenu.getId().equals(permission.getParentId())));
    }
}
