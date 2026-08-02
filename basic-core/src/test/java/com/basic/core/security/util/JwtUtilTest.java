package com.basic.core.security.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JWT 动态有效期测试。
 */
class JwtUtilTest {

    @Test
    void generateTokenShouldUseProvidedExpireMillis() {
        long expireMillis = TimeUnit.HOURS.toMillis(6);
        String token = JwtUtil.generateToken(10L, "admin", List.of("研发部"), expireMillis);

        Claims claims = JwtUtil.parseToken(token);

        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(expireMillis);
    }

    @Test
    void generateTokenShouldRejectNonPositiveExpiration() {
        assertThatThrownBy(() -> JwtUtil.generateToken(10L, "admin", List.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
