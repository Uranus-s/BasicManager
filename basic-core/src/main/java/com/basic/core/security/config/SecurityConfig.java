package com.basic.core.security.config;

import com.basic.core.security.filter.JwtAuthenticationFilter;
import com.basic.core.security.handler.AuthenticationEntryPointImpl;
import com.basic.core.security.handler.AccessDeniedHandlerImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
     * 本地上传文件访问前缀。
     * <p>
     * Web 层的 LocalUploadResourceConfig 只负责把本地目录映射成静态资源；
     * Security 仍会拦截所有未放行的 URL。因此这里必须使用同一个 url-prefix 生成白名单，
     * 否则浏览器直接访问头像地址时会返回“未登录或登录已过期”。
     * </p>
     */
    @Value("${basic.file.local.url-prefix:/uploads}")
    private String uploadUrlPrefix;

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
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(openApiRequestMatchers())
                        .permitAll()
                        .requestMatchers(
                                "/auth/login",
                                "/auth/initAdmin",
                                "/auth/register",
                                "/auth/forgotPassword/reset",
                                "/captcha",
                                "/public/**",
                                // 放行本地上传文件访问，例如 /uploads/avatar/2026/05/27/xxx.jpg。
                                localUploadPattern(),
                                "/test/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())

                // 加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private String localUploadPattern() {
        // 与 LocalUploadResourceConfig 保持同样的前缀规范化规则，避免一个能映射、另一个不能放行。
        String prefix = uploadUrlPrefix == null || uploadUrlPrefix.isBlank() ? "/uploads" : uploadUrlPrefix.replace("\\", "/");
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/**";
    }

    /**
     * OpenAPI 文档和 Swagger UI 的公开访问路径。
     *
     * @return 文档端点匹配规则
     */
    static String[] openApiRequestMatchers() {
        return new String[] {
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/swagger-ui/**",
                "/swagger-ui.html"
        };
    }
}
