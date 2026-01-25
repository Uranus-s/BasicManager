package com.basic.core.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义访问拒绝处理器实现类
 * 实现Spring Security的AccessDeniedHandler接口，用于处理用户无权限访问资源时的响应
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private static final int FORBIDDEN_STATUS_CODE = 403;
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String ERROR_RESPONSE = "{\"code\":403,\"msg\":\"没有访问权限\"}";

    /**
     * 处理访问拒绝异常
     * 当用户尝试访问没有权限的资源时，此方法会被调用并返回统一的错误响应格式
     *
     * @param request HTTP请求对象，包含客户端的请求信息
     * @param response HTTP响应对象，用于向客户端发送响应数据
     * @param ex 访问被拒绝时抛出的异常对象
     * @throws IOException 写入响应数据时可能发生的IO异常
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        // 设置HTTP响应状态码为403（禁止访问）
        response.setStatus(FORBIDDEN_STATUS_CODE);
        // 设置响应内容类型为JSON格式
        response.setContentType(CONTENT_TYPE);

        try (PrintWriter writer = response.getWriter()) {
            writer.write(ERROR_RESPONSE);
            writer.flush();
        } catch (IOException e) {
            // 记录异常日志，但不抛出，因为响应已经设置状态码
            throw e;
        }
    }
}
