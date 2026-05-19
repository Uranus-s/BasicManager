package com.basic.sericve.auth.service;

import com.basic.api.vo.auth.LoginVO;
import com.basic.api.vo.auth.OnlineUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IAuthService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    default LoginVO login(String username, String password) {
        return login(username, password, null);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param request  HTTP请求
     * @return 登录结果
     */
    LoginVO login(String username, String password, HttpServletRequest request);

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
