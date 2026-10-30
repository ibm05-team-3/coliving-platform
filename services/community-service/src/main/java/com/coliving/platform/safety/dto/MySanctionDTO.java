package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.SanctionType;
import java.time.LocalDateTime;

/** 8-5 내 이용 제한 상태 · 5-9 sanction */
public record MySanctionDTO(
		boolean active,
		Long sanctionId,
		SanctionType sanctionType,
		Integer durationDays,
		String reason,
		LocalDateTime startsAt,
		LocalDateTime endsAt
) {

	public static MySanctionDTO none() {
		return new MySanctionDTO(false, null, null, null, null, null, null);
	}
}
