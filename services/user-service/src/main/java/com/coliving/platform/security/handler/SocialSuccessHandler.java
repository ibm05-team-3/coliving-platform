package com.coliving.platform.security.handler;

import com.coliving.platform.common.config.AppProperties;
import com.coliving.platform.security.dto.AuthTokenResponseDTO;
import com.coliving.platform.security.service.AuthService;
import com.coliving.platform.security.config.AuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SocialSuccessHandler implements AuthenticationSuccessHandler {
    public static final String REDIRECT_LOCATION = "/cookie";

    private final AuthService authService;
    private final AppProperties appProperties;
    private final AuthProperties authProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // authentication.getName() => CustomOAuth2User 의 getName 과 동일한 값
        String sub = authentication.getName();

        AuthTokenResponseDTO tokens = authService.issueTokens(sub);

        ResponseCookie refreshCookie = refreshCookie(tokens.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        response.sendRedirect(appProperties.frontend().url() + REDIRECT_LOCATION);
    }

    public ResponseCookie refreshCookie(String rawRefreshToken) {
        return ResponseCookie.from(appProperties.cookie().refreshTokenName(), rawRefreshToken)
                .httpOnly(true)
                .secure(appProperties.cookie().secure())
                .path("/")
                .maxAge(authProperties.refreshTokenExpiration())
                .sameSite(appProperties.cookie().sameSite())
                .build();
    }
}