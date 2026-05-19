package com.basic.api.controller;

import com.basic.api.dto.auth.InitAdminDTO;
import com.basic.api.dto.auth.LoginDTO;
import com.basic.api.vo.auth.InitResultVO;
import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 认证 API 接口
 *
 * @author Gas
 */
public interface AuthApi {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return 登录结果（包含Token和用户信息）
     */
    Result<LoginVO> login(LoginDTO loginDTO, HttpServletRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    Result<LoginVO> getUserInfo();

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization);

    /**
     * 获取当前在线用户列表
     *
     * @return 在线用户列表
     */
    Result<List<OnlineUserVO>> getOnlineUsers();

    /**
     * 强制用户下线
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<?> forceLogout(@PathVariable("userId") Long userId);

    /**
     * 初始化管理员
     *
     * @param initAdminDTO 初始化请求
     * @return 初始化结果
     */
    Result<InitResultVO> initAdmin(InitAdminDTO initAdminDTO);

    /**
     * 获取当前用户路由权限
     *
     * @return 路由权限树形列表
     */
    Result<List<PermissionTreeVO>> getUserRoutes();
}
