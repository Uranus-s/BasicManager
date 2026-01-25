package com.basic.core.security.model;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户实体类，实现Spring Security的UserDetails接口
 * 用于封装用户认证和授权相关信息
 */
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    private String password;
    private boolean enabled;

    /**
     * 获取用户权限集合
     * @return 用户拥有的权限列表
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 先不做权限
        return List.of();
    }

    /**
     * 获取用户密码
     * @return 用户密码
     */
    @Override
    public @Nullable String getPassword() {
        return password;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 判断账户是否未过期
     * @return 账户是否未过期，默认使用父类实现
     */
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    /**
     * 判断账户是否未被锁定
     * @return 账户是否未被锁定，默认使用父类实现
     */
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    /**
     * 判断凭证是否未过期
     * @return 凭证是否未过期，默认使用父类实现
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    /**
     * 判断用户是否启用
     * @return 用户是否启用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

