package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.ReportStatus;
import java.time.LocalDateTime;

/** 8-1 응답 */
public record ReportCreateResponseDTO(
		Long reportId,
		ReportStatus status,
		LocalDateTime createdAt
) {
}
