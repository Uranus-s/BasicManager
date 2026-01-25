package com.basic.core.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证入口点实现类，用于处理未认证用户的请求
 * 当用户未登录或登录过期时，返回统一的错误响应
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private static final String ERROR_MESSAGE = "未登录或登录已过期";
    private static final int UNAUTHORIZED_CODE = 401;
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String CHARSET = "UTF-8";

    /**
     * 处理未认证的请求，返回401未授权响应
     *
     * @param request HTTP请求对象，包含客户端的请求信息
     * @param response HTTP响应对象，用于向客户端发送响应
     * @param authException 认证异常对象，包含认证失败的具体原因
     * @throws IOException 当写入响应流发生错误时抛出
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(CHARSET);
        response.setContentType(CONTENT_TYPE);

        String json = "{\"code\":" + UNAUTHORIZED_CODE + ",\"msg\":\"" + ERROR_MESSAGE + "\"}";

        try {
            response.getWriter().write(json);
            response.getWriter().flush();
        } catch (IOException e) {
            // 记录异常日志，但不抛出，因为响应已经设置状态码
        }
    }
}

