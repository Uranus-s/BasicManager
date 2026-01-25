package com.basic.core.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类，用于生成、解析和验证JWT令牌
 */
public class JwtUtil {

    /**
     * JWT密钥，从系统属性获取，如果未设置则使用默认值
     */
    private static final String SECRET = System.getProperty("jwt.secret", "abc123!@#abc123!@#abc123!@#abc123!@#");

    /**
     * Token过期时间，单位毫秒（24小时）
     */
    private static final long EXPIRE = 24 * 60 * 60 * 1000L;

    /**
     * 根据SECRET字符串生成HMAC SHA密钥
     */
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID，不能为空
     * @param username 用户名，不能为空
     * @return 生成的JWT令牌字符串
     * @throws IllegalArgumentException 当用户ID或用户名为空时抛出异常
     */
    public static String generateToken(Long userId, String username) {
        if (userId == null || username == null) {
            throw new IllegalArgumentException("User ID and username cannot be null");
        }
        return Jwts.builder().subject(username).claim("uid", userId).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + EXPIRE)).signWith(KEY, Jwts.SIG.HS256).compact();
    }

    /**
     * 解析JWT令牌并返回其中的声明信息
     *
     * @param token JWT令牌字符串，不能为空
     * @return 包含令牌中声明信息的Claims对象
     * @throws RuntimeException 当令牌解析失败时抛出运行时异常
     */
    public static Claims parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        try {
            return Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token: " + e.getMessage(), e);
        }
    }

    /**
     * 检查JWT令牌是否过期
     *
     * @param token JWT令牌字符串
     * @return 如果令牌过期或无效则返回true，否则返回false
     */
    public static boolean isExpired(String token) {
        if (token == null || token.trim().isEmpty()) {
            return true;
        }
        try {
            Date exp = parseToken(token).getExpiration();
            return exp.before(new Date());
        } catch (Exception e) {
            // 如果无法解析token，则认为已过期或无效
            return true;
        }
    }
}

