package com.basic.core.log.aspect;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志切面请求工具。
 */
final class LogRequestUtils {

    private static final int MAX_LENGTH = 2000;

    private LogRequestUtils() {
    }

    static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    static String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            values.add(mask(String.valueOf(arg)));
        }
        return limit(values.toString());
    }

    static String serializeResult(Object result) {
        return limit(mask(String.valueOf(result)));
    }

    static String extractUsername(Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            return "";
        }
        Object loginArg = args[0];
        try {
            Method method = loginArg.getClass().getMethod("getUsername");
            Object username = method.invoke(loginArg);
            return username == null ? "" : String.valueOf(username);
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }

    static String getIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return ip == null || ip.isBlank() ? request.getRemoteAddr() : ip;
    }

    static String getBrowser(HttpServletRequest request) {
        String userAgent = request == null ? "" : request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return "";
        }
        if (userAgent.contains("Edg")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari")) {
            return "Safari";
        }
        return "Unknown";
    }

    static String getOs(HttpServletRequest request) {
        String userAgent = request == null ? "" : request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return "";
        }
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Mac OS")) {
            return "Mac OS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        if (userAgent.contains("Android")) {
            return "Android";
        }
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        return "Unknown";
    }

    private static String mask(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("(?i)(password|token|secret)=([^,}\\]]+)", "$1=******");
    }

    private static String limit(String text) {
        if (text == null || text.length() <= MAX_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_LENGTH);
    }
}
