package com.coliving.platform.security.session;

public record BrowserSession(
        String sub,
        String refreshToken,
        String accessToken,
        long accessExpiresAtEpochMilli
) {
}
