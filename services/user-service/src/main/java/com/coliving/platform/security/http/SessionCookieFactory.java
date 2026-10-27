package com.coliving.platform.security.http;

import com.coliving.platform.common.config.AppProperties;
import com.coliving.platform.security.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionCookieFactory {

    private final AppProperties appProperties;
    private final AuthProperties authProperties;

    public ResponseCookie write(String sessionId) {
        return base(sessionId)
                .maxAge(authProperties.refreshTokenExpiration())
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
        String name = appProperties.cookie().refreshTokenName();
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        // refreshTokenName is the configured cookie name. The value is a session id.
        return ResponseCookie.from(appProperties.cookie().refreshTokenName(), value)
                .httpOnly(true)
                .secure(appProperties.cookie().secure())
                .path("/")
                .sameSite(appProperties.cookie().sameSite());
    }
}
