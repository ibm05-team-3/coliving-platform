package com.coliving.platform.safety.dto;

import java.time.LocalDateTime;

/** 9-5 응답 */
public record SanctionRevokeResponseDTO(
		Long sanctionId,
		LocalDateTime revokedAt
) {
}
