package com.coliving.platform.safety.dto;

import java.time.LocalDateTime;

/** 8-2 차단 목록 항목 */
public record BlockedUserDTO(
		Long blockedUserId,
		String name,
		String roomNumber,
		String profileImageUrl,
		LocalDateTime blockedAt
) {
}
