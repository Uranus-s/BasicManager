package com.basic.web.controller;

import com.basic.api.controller.AuthApi;
import com.basic.api.dto.auth.InitAdminDTO;
import com.basic.api.dto.auth.LoginDTO;
import com.basic.api.vo.auth.InitResultVO;
import com.basic.api.vo.auth.LoginVO;
import com.basic.common.result.Result;
import com.basic.core.security.model.LoginUser;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 Controller
 *
 * @author Gas
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final ISysUserService sysUserService;

    @Value("${system.init-key:admin-init-key}")
    private String initKey;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息（用户名、密码）
     * @return 登录结果（Token和用户信息）
     */
    @Override
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success(loginVO);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @Override
    public Result<LoginVO> getUserInfo() {
        // 从SecurityContextHolder获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.build(false, com.basic.common.result.ResultEnum.UNAUTHORIZED.getCode(),
                    com.basic.common.result.ResultEnum.UNAUTHORIZED.getMessage(), null);
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(loginUser.getUserId());
        loginVO.setUsername(loginUser.getUsername());
        loginVO.setNickname(loginUser.getNickname());
        loginVO.setAvatar(loginUser.getAvatar());
        loginVO.setRoles(loginUser.getRoles());
        loginVO.setPermissions(loginUser.getPermissions());

        return Result.success(loginVO);
    }

    /**
     * 用户登出
     *
     * @return 操作结果
     */
    @Override
    public Result<?> logout() {
        // Security会自动处理登出，清除SecurityContext即可
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    /**
     * 初始化管理员
     *
     * @param initAdminDTO 初始化请求
     * @return 初始化结果
     */
    @Override
    public Result<InitResultVO> initAdmin(@Valid @RequestBody InitAdminDTO initAdminDTO) {
        // 验证密钥
        if (!initKey.equals(initAdminDTO.getInitKey())) {
            return Result.build(false, 401, "初始化密钥错误", null);
        }

        // 执行初始化
        InitResultVO result = sysUserService.initAdmin(initAdminDTO.getAdminPassword());
        return Result.success(result);
    }
}
