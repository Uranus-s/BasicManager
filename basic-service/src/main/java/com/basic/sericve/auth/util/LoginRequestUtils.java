package com.basic.sericve.auth.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录请求元数据解析工具。
 */
public final class LoginRequestUtils {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String USER_AGENT = "User-Agent";

    private LoginRequestUtils() {
    }

    public static String resolveMaskedIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader(X_FORWARDED_FOR);
        if (ip != null && !ip.isBlank()) {
            return maskIp(ip.split(",", -1)[0]);
        }
        ip = request.getHeader(X_REAL_IP);
        if (ip != null && !ip.isBlank()) {
            return maskIp(ip);
        }
        return maskIp(request.getRemoteAddr());
    }

    public static String resolveBrowser(HttpServletRequest request) {
        String userAgent = userAgent(request);
        if (userAgent.isBlank()) {
            return "Other";
        }
        if (containsAny(userAgent, "Edg/", "Edge/", "EdgA/", "EdgiOS/")) {
            return "Edge";
        }
        if (containsAny(userAgent, "Chrome/", "CriOS/")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari/")) {
            return "Safari";
        }
        return "Other";
    }

    public static String resolveOs(HttpServletRequest request) {
        String userAgent = userAgent(request);
        if (userAgent.isBlank()) {
            return "Other";
        }
        if (containsAny(userAgent, "Android")) {
            return "Android";
        }
        if (containsAny(userAgent, "iPhone", "iPad", "iPod")) {
            return "iOS";
        }
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (containsAny(userAgent, "Mac OS X", "Macintosh")) {
            return "macOS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "Other";
    }

    public static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "";
        }
        String trimmed = ip.trim();
        if (trimmed.contains(":")) {
            return maskIpv6(trimmed);
        }
        String[] segments = trimmed.split("\\.", -1);
        if (segments.length != 4 || !isIpv4Segment(segments[0]) || !isIpv4Segment(segments[1])
                || !isIpv4Segment(segments[2]) || !isIpv4Segment(segments[3])) {
            return trimmed;
        }
        return segments[0] + "." + segments[1] + ".*." + segments[3];
    }

    private static String userAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String userAgent = request.getHeader(USER_AGENT);
        return userAgent == null ? "" : userAgent;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIpv4Segment(String segment) {
        if (segment == null || segment.isEmpty() || segment.length() > 3) {
            return false;
        }
        for (int i = 0; i < segment.length(); i++) {
            if (!Character.isDigit(segment.charAt(i))) {
                return false;
            }
        }
        int value = Integer.parseInt(segment);
        return value >= 0 && value <= 255;
    }

    private static String maskIpv6(String ip) {
        String[] segments = ip.split(":", -1);
        String first = "";
        String last = "";
        for (String segment : segments) {
            if (!segment.isBlank()) {
                if (first.isEmpty()) {
                    first = segment;
                }
                last = segment;
            }
        }
        if (first.isEmpty()) {
            return "*";
        }
        if (first.equals(last)) {
            return "*:" + last;
        }
        return first + ":*:" + last;
    }
}
