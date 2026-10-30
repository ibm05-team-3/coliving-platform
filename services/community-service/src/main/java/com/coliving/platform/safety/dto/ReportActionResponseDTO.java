package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.ReportAction;
import com.coliving.platform.safety.entity.ReportStatus;
import java.time.LocalDateTime;

/** 9-3 응답 */
public record ReportActionResponseDTO(
		Long reportId,
		int handledReportCount,
		ReportStatus status,
		ReportAction action,
		Long sanctionId,
		LocalDateTime endsAt,
		LocalDateTime handledAt
) {
}
