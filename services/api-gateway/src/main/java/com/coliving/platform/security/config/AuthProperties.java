package com.coliving.platform.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        String accessTokenPublic,
        String accessTokenKeyId,
        String accessTokenIssuer
) {
}