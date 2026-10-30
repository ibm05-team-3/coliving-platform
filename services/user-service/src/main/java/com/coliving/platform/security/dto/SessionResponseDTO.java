package com.coliving.platform.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionResponseDTO(
        boolean authenticated,
        String userId,
        String nickname
) {
}
