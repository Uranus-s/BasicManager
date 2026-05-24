package com.basic.core.security.spi;

import com.basic.core.security.model.LoginUser;

import java.util.List;

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

    /**
     * 根据用户ID加载登录用户信息
     *
     * @param userId 用户ID
     * @return LoginUser 返回与用户ID匹配的用户对象，如果用户不存在则返回null
     */
    LoginUser loadByUserId(Long userId);

    /**
     * 查询系统中全部权限标识，用于开发环境调试 Token 构造完整权限上下文。
     *
     * @return 全部权限标识列表
     */
    List<String> listAllPermissionCodes();
}
