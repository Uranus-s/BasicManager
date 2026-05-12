package com.basic.sericve.auth.service;

import com.basic.api.vo.auth.LoginVO;

public interface IAuthService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    LoginVO login(String username, String password);

    /**
     * 用户登出
     *
     * @param token 登录Token
     */
    void logout(String token);
}
