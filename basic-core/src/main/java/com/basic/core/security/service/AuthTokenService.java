package com.basic.core.security.service;

import com.basic.core.redis.utils.RedisUtils;
import com.basic.core.security.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class AuthTokenService {

    private static final String LOGIN_TOKEN_KEY_PREFIX = "login:token:";

    private final RedisUtils redisUtils;

    public AuthTokenService(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public void saveLoginToken(Long userId, String token) {
        redisUtils.set(buildLoginTokenKey(userId), token, JwtUtil.getExpireMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean isCurrentLoginToken(Long userId, String token) {
        if (userId == null || !StringUtils.hasText(token)) {
            return false;
        }
        return Objects.equals(redisUtils.get(buildLoginTokenKey(userId)), token);
    }

    public void deleteLoginToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        Long userId = JwtUtil.getUserId(token);
        redisUtils.delete(buildLoginTokenKey(userId));
    }

    private String buildLoginTokenKey(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return LOGIN_TOKEN_KEY_PREFIX + userId;
    }
}
