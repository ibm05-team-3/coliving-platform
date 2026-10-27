package com.coliving.platform.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Frontend frontend,
        Cookie cookie
) {
    public record Frontend(
            String url
    ) {}

    /**
     * refreshTokenName is the HttpOnly session cookie name.
     * The cookie value is a session id. Access and refresh tokens stay on the server.
     */
    public record Cookie(
            boolean secure,
            String sameSite,
            String refreshTokenName
    ) {}
}