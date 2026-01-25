package com.basic.core.security.service;

import com.basic.core.security.spi.SecurityUserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * 安全用户详细信息服务实现类
 * 实现Spring Security的UserDetailsService接口，用于根据用户名加载用户详细信息
 */
@Component
public class SecurityUserDetailsService implements UserDetailsService {

    /**
     * 注入安全用户查询服务，用于执行具体的用户查询逻辑
     */
    @Autowired
    private SecurityUserQueryService securityUserQueryService;

    /**
     * 根据用户名加载用户详细信息
     * 实现UserDetailsService接口的loadUserByUsername方法
     *
     * @param username 用户名，用于查询对应的用户信息
     * @return UserDetails 包含用户认证和授权信息的用户详情对象
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        return securityUserQueryService.loadByUsername(username);
    }
}
