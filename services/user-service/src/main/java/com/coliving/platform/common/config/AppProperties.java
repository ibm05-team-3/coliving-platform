package com.coliving.platform.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Frontend frontend,
        Cookie cookie
) {
    public record Frontend(
            String url
    ) {}

    /**
     * sessionCookieName is the HttpOnly session cookie name.
     * The cookie value is a session id. Access and refresh tokens stay on the server.
     * oauthTransactionName is the short-lived cookie used by every social provider
     * while the browser is at the provider. Its value is a Redis key id.
     */
    public record Cookie(
            boolean secure,
            String sameSite,
            String sessionCookieName,
            @DefaultValue("oauth_tx") String oauthTransactionName,
            @DefaultValue("10m") Duration oauthTransactionMaxAge
    ) {}
}