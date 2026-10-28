package com.coliving.platform.security.http;

import com.coliving.platform.common.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuthTransactionCookie {

    private final AppProperties appProperties;

    public ResponseCookie write(String transactionId) {
        return base(transactionId)
                .maxAge(appProperties.cookie().oauthTransactionMaxAge())
                .build();
    }

    public ResponseCookie clear() {
        return base("")
                .maxAge(0)
                .build();
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        String name = appProperties.cookie().oauthTransactionName();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        // The provider returns with a cross-site top-level GET, so this cookie stays Lax
        // even when the session cookie is Strict.
        return ResponseCookie.from(appProperties.cookie().oauthTransactionName(), value)
                .httpOnly(true)
                .secure(appProperties.cookie().secure())
                .path("/")
                .sameSite("Lax");
    }
}
