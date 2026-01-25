package com.basic.core.security.config;

import com.basic.core.security.filter.JwtAuthenticationFilter;
import com.basic.core.security.handler.AuthenticationEntryPointImpl;
import com.basic.core.security.handler.AccessDeniedHandlerImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 * 负责配置安全过滤器链、认证授权规则等安全相关设置
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * JWT 认证过滤器
     * 用于在请求中验证 JWT token 的有效性
     */
    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 认证入口点实现
     * 当未认证用户访问受保护资源时的处理逻辑
     */
    @Resource
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    /**
     * 访问拒绝处理器实现
     * 当已认证用户但权限不足时的处理逻辑
     */
    @Resource
    private AccessDeniedHandlerImpl accessDeniedHandler;

    /**
     * 配置安全过滤器链
     * 定义了应用的安全策略，包括 CSRF 防护、会话管理、异常处理等
     *
     * @param http HttpSecurity 对象，用于构建安全配置
     * @return SecurityFilterChain 安全过滤器链实例
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 前后端分离必须关
                .csrf(csrf -> csrf.disable())

                // 不使用 session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 异常处理
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler))

                // 关闭表单登录
                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())

                // 权限规则
                .authorizeHttpRequests(auth -> auth.requestMatchers("/login", "/captcha", "/public/**").permitAll().anyRequest().authenticated())

                // 加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
