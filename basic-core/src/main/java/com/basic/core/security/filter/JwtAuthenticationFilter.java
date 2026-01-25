package com.basic.core.security.filter;

import com.basic.core.security.service.SecurityUserDetailsService;
import com.basic.core.security.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT身份验证过滤器
 * 继承OncePerRequestFilter确保每次HTTP请求只执行一次过滤操作
 * 从请求头中提取JWT令牌并进行验证，设置Spring Security上下文中的认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private SecurityUserDetailsService userDetailsService;

    /**
     * 内部过滤方法，处理JWT令牌验证逻辑
     * @param req HTTP请求对象
     * @param resp HTTP响应对象
     * @param chain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String token = req.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();

            try {
                Claims claims = JwtUtil.parseToken(token);
                if (claims != null) {
                    String username = claims.getSubject();

                    if (username != null && !username.isEmpty()) {
                        UserDetails user = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                // 记录无效令牌的日志，但不中断请求流程
                // 这样允许无认证的公共接口正常工作
            }
        }

        chain.doFilter(req, resp);
    }
}

