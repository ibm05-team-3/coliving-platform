package com.coliving.platform.security.dao;

import com.coliving.platform.common.config.AppProperties;
import com.coliving.platform.security.oauth.OAuthTransaction;
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
public class OAuthTransactionDAO {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final String KEY_PREFIX = "auth:oauth-tx:";

    private final AppProperties appProperties;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String newId() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void save(String id, OAuthTransaction transaction) {
        redis.opsForValue().set(
                KEY_PREFIX + id,
                write(transaction),
                appProperties.cookie().oauthTransactionMaxAge()
        );
    }

    public Optional<OAuthTransaction> find(String id) {
        return read(id, redis.opsForValue().get(KEY_PREFIX + id));
    }

    public Optional<OAuthTransaction> findAndDelete(String id) {
        return read(id, redis.opsForValue().getAndDelete(KEY_PREFIX + id));
    }

    public void delete(String id) {
        redis.delete(KEY_PREFIX + id);
    }

    private Optional<OAuthTransaction> read(String id, String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, OAuthTransaction.class));
        } catch (JsonProcessingException ex) {
            redis.delete(KEY_PREFIX + id);
            return Optional.empty();
        }
    }

    private String write(OAuthTransaction transaction) {
        try {
            return objectMapper.writeValueAsString(transaction);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("oauth transaction serialize failed", ex);
        }
    }
}
