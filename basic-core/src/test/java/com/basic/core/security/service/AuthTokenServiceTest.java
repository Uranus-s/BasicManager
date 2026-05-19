package com.basic.core.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.basic.core.redis.utils.RedisUtils;
import com.basic.core.security.model.LoginSession;
import com.basic.core.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthTokenServiceTest {

    private RedisUtils redisUtils;
    private AuthTokenService authTokenService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        redisUtils = mock(RedisUtils.class);
        objectMapper = new ObjectMapper();
        authTokenService = new AuthTokenService(redisUtils);
    }

    @Test
    void savesLoginSessionUsingJwtExpireMillis() {
        LoginSession session = buildSession(10L, "token-value");

        authTokenService.saveLoginSession(session);

        ArgumentCaptor<String> sessionCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtils).set(eq("login:session:10"), sessionCaptor.capture(),
                eq(JwtUtil.getExpireMillis()), eq(TimeUnit.MILLISECONDS));
        assertThat(sessionCaptor.getValue()).contains("\"token\":\"token-value\"");
    }

    @Test
    void savesTokenAsLoginSessionForCompatibility() {
        authTokenService.saveLoginToken(10L, "token-value");

        ArgumentCaptor<String> sessionCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtils).set(eq("login:session:10"), sessionCaptor.capture(), eq(JwtUtil.getExpireMillis()), eq(TimeUnit.MILLISECONDS));
        LoginSession session = readSession(sessionCaptor.getValue());
        assertThat(session.getUserId()).isEqualTo(10L);
        assertThat(session.getToken()).isEqualTo("token-value");
    }

    @Test
    void validatesCurrentTokenBySessionToken() {
        when(redisUtils.get("login:session:10")).thenReturn(writeSession(buildSession(10L, "current-token")));

        assertThat(authTokenService.isCurrentLoginToken(10L, "current-token")).isTrue();
        assertThat(authTokenService.isCurrentLoginToken(10L, "old-token")).isFalse();
    }

    @Test
    void validatesCurrentTokenByLegacyStringValue() {
        when(redisUtils.get("login:session:10")).thenReturn("current-token");

        assertThat(authTokenService.isCurrentLoginToken(10L, "current-token")).isTrue();
        assertThat(authTokenService.isCurrentLoginToken(10L, "old-token")).isFalse();
    }

    @Test
    void deletesTokenByParsedUserId() {
        String token = JwtUtil.generateToken(10L, "admin");

        authTokenService.deleteLoginToken(token);

        verify(redisUtils).delete("login:session:10");
    }

    @Test
    void forceLogoutDeletesLoginSession() {
        authTokenService.forceLogout(10L);

        verify(redisUtils).delete("login:session:10");
    }

    @Test
    void listsOnlineSessionsAndIgnoresOtherValues() {
        LoginSession session = buildSession(10L, "token-value");
        when(redisUtils.keys("login:session:*")).thenReturn(Set.of("login:session:10", "login:session:11"));
        when(redisUtils.get("login:session:10")).thenReturn(session);
        when(redisUtils.get("login:session:11")).thenReturn("legacy-token");

        assertThat(authTokenService.listOnlineSessions()).containsExactly(session);
    }

    private String writeSession(LoginSession session) {
        return """
                {"token":"%s","userId":%d,"username":"admin","nickname":"管理员","avatar":"avatar.png","loginTime":"2026-05-12T10:00:00","loginIp":"127.0.0.1","browser":"Chrome","os":"Windows"}"""
                .formatted(session.getToken(), session.getUserId());
    }

    private LoginSession readSession(String value) {
        try {
            return objectMapper.readValue(value, LoginSession.class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private LoginSession buildSession(Long userId, String token) {
        LoginSession session = new LoginSession();
        session.setToken(token);
        session.setUserId(userId);
        session.setUsername("admin");
        session.setNickname("管理员");
        session.setAvatar("avatar.png");
        session.setLoginTime(LocalDateTime.of(2026, 5, 12, 10, 0));
        session.setLoginIp("127.0.0.1");
        session.setBrowser("Chrome");
        session.setOs("Windows");
        return session;
    }
}
