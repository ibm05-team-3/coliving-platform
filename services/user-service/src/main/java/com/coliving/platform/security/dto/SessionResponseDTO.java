package com.coliving.platform.security.dto;

public record SessionResponseDTO(
        boolean authenticated,
        String userId
) {
}
