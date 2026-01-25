package com.basic.core.security.spi;

import com.basic.core.security.model.LoginUser;

/**
 * 安全用户查询服务接口
 * 提供用户安全相关的查询功能
 */
public interface SecurityUserQueryService {
    /**
     * 根据用户名加载用户信息
     *
     * @param username 用户名，用于查询对应的用户信息
     * @return LoginUser 返回与用户名匹配的用户对象，如果用户不存在则返回null
     */
    LoginUser loadByUsername(String username);
}
