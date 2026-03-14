package com.basic.api.controller;

import com.basic.api.dto.auth.InitAdminDTO;
import com.basic.api.dto.auth.LoginDTO;
import com.basic.api.vo.auth.InitResultVO;
import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.sysPermission.PermissionTreeVO;
import com.basic.common.result.Result;

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
    Result<LoginVO> login(LoginDTO loginDTO);

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
    Result<?> logout();

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
