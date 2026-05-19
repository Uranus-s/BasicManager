package com.basic.core.security.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.basic.core.redis.utils.RedisUtils;
import com.basic.core.security.model.LoginSession;
import com.basic.core.security.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class AuthTokenService {

    private static final String LOGIN_SESSION_KEY_PREFIX = "login:session:";
    private static final String LOGIN_SESSION_KEY_PATTERN = LOGIN_SESSION_KEY_PREFIX + "*";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    public AuthTokenService(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
        this.objectMapper = new ObjectMapper().registerModule(loginSessionTimeModule());
    }

    public void saveLoginSession(LoginSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Login session cannot be null");
        }
        redisUtils.set(buildLoginSessionKey(session.getUserId()), writeSession(session),
                JwtUtil.getExpireMillis(), TimeUnit.MILLISECONDS);
    }

    public void saveLoginToken(Long userId, String token) {
        LoginSession session = new LoginSession();
        session.setUserId(userId);
        session.setToken(token);
        saveLoginSession(session);
    }

    public boolean isCurrentLoginToken(Long userId, String token) {
        if (userId == null || !StringUtils.hasText(token)) {
            return false;
        }
        Object value = redisUtils.get(buildLoginSessionKey(userId));
        LoginSession session = readSession(value);
        if (session != null) {
            return Objects.equals(session.getToken(), token);
        }
        return Objects.equals(value, token);
    }

    public void deleteLoginToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        Long userId = JwtUtil.getUserId(token);
        forceLogout(userId);
    }

    public void forceLogout(Long userId) {
        redisUtils.delete(buildLoginSessionKey(userId));
    }

    public List<LoginSession> listOnlineSessions() {
        Set<String> keys = redisUtils.keys(LOGIN_SESSION_KEY_PATTERN);
        List<LoginSession> sessions = new ArrayList<>();
        for (String key : keys) {
            LoginSession session = readSession(redisUtils.get(key));
            if (session != null) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    private String writeSession(LoginSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize login session", e);
        }
    }

    private LoginSession readSession(Object value) {
        if (value instanceof LoginSession session) {
            return session;
        }
        if (value instanceof String text && text.trim().startsWith("{")) {
            try {
                return objectMapper.readValue(text, LoginSession.class);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        return null;
    }

    private String buildLoginSessionKey(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return LOGIN_SESSION_KEY_PREFIX + userId;
    }

    private SimpleModule loginSessionTimeModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.format(DATE_TIME_FORMATTER));
            }
        });
        module.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return LocalDateTime.parse(p.getValueAsString(), DATE_TIME_FORMATTER);
            }
        });
        return module;
    }
}
