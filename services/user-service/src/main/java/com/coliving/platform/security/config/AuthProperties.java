package com.coliving.platform.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        String accessTokenPublic,
        String accessTokenPrivate,
        String accessTokenKeyId,
        String accessTokenIssuer,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration,
        Duration accessTokenRefreshSkew
) {
}