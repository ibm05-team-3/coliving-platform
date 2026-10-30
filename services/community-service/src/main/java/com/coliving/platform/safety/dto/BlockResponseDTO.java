package com.coliving.platform.safety.dto;

import java.time.LocalDateTime;

/** 8-3 응답 */
public record BlockResponseDTO(Long blockedUserId, LocalDateTime blockedAt) {
}
