package com.basic.sericve.auth.service;

import com.basic.api.dto.auth.ForgotPasswordResetDTO;
import com.basic.api.dto.auth.RegisterDTO;
import com.basic.api.vo.auth.OnlineUserVO;
import com.basic.api.vo.auth.TokenVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IAuthService {

    /**
     * 注册账号
     *
     * @param registerDTO 注册请求
     * @return 用户ID
     */
    Long register(RegisterDTO registerDTO);

    /**
     * 忘记密码重置密码
     *
     * @param resetDTO 重置请求
     */
    void resetForgottenPassword(ForgotPasswordResetDTO resetDTO);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return Token 信息
     */
    default TokenVO login(String username, String password) {
        return login(username, password, null);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param request  HTTP请求
     * @return Token 信息
     */
    TokenVO login(String username, String password, HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param token 登录Token
     */
    void logout(String token);

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    List<OnlineUserVO> getOnlineUsers();

    /**
     * 强制用户下线
     *
     * @param userId 用户ID
     */
    void forceLogout(Long userId);
}
