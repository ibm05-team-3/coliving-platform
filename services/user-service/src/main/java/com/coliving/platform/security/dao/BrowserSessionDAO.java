package com.coliving.platform.security.dao;

import com.coliving.platform.security.config.AuthProperties;
import com.coliving.platform.security.session.BrowserSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BrowserSessionDAO {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final String KEY_PREFIX = "auth:session:";

    private final AuthProperties authProperties;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String saveNew(BrowserSession session) {
        String sessionId = newSessionId();
        save(sessionId, session);
        return sessionId;
    }

    public void save(String sessionId, BrowserSession session) {
        redis.opsForValue().set(
                KEY_PREFIX + sessionId,
                write(session),
                authProperties.refreshTokenExpiration()
        );
    }

    public Optional<BrowserSession> find(String sessionId) {
        String json = redis.opsForValue().get(KEY_PREFIX + sessionId);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, BrowserSession.class));
        } catch (JsonProcessingException ex) {
            redis.delete(KEY_PREFIX + sessionId);
            return Optional.empty();
        }
    }

    public void delete(String sessionId) {
        redis.delete(KEY_PREFIX + sessionId);
    }

    private String write(BrowserSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("session serialize failed", ex);
        }
    }

    private static String newSessionId() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
