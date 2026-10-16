package com.coliving.platform.security.dto;

public record AuthTokenResponseDTO(
        String accessToken,
        String refreshToken
) {
    public static AuthTokenResponseDTO of(String accessToken, String refreshToken) {
        return new AuthTokenResponseDTO(accessToken, refreshToken);
    }
}
