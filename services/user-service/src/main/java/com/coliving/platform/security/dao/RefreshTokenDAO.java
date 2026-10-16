package com.coliving.platform.security.dao;

import com.coliving.platform.security.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenDAO {

    private final AuthProperties authProperties;
    private final StringRedisTemplate redis;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final String KEY_PREFIX = "auth:refresh:";

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static String generateOpaqueRefreshToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * 로그인 / 토큰 회전 시 호출
     */
    public String generateAndSave(String accessSub) {
        String newRawRefresh = generateOpaqueRefreshToken();
        String newHash = hash(newRawRefresh);
        redis.opsForValue().set(KEY_PREFIX + newHash, accessSub, authProperties.refreshTokenExpiration());
        return newRawRefresh;
    }

    /**
     * 로그아웃 / 회전 후 기존 토큰 폐기
     */
    public void deleteTokenHash(String rawToken) {
        redis.delete(KEY_PREFIX + hash(rawToken));
    }

    /**
     * 동시 회전 요청 race condition 방지에 유용
     */
    public Optional<String> findAndDelete(String rawToken) {
        String accessSub = redis.opsForValue().getAndDelete(KEY_PREFIX + hash(rawToken));
        return Optional.ofNullable(accessSub);
    }
}