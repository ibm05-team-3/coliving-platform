package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.SanctionType;
import java.time.LocalDateTime;

/** 9-4 제재 목록 항목 */
public record AdminSanctionItemDTO(
		Long sanctionId,
		Long userId,
		String userName,
		String roomNumber,
		String buildingName,
		SanctionType sanctionType,
		Integer durationDays,
		String reason,
		Long reportId,
		LocalDateTime startsAt,
		LocalDateTime endsAt,
		boolean active,
		String createdByName,
		LocalDateTime revokedAt,
		String revokeReason
) {
}
