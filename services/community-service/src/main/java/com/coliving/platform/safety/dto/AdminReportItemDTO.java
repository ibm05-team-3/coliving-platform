package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.ReportReason;
import com.coliving.platform.safety.entity.ReportSourceType;
import com.coliving.platform.safety.entity.ReportStatus;
import com.coliving.platform.safety.entity.ReportTargetType;
import java.time.LocalDateTime;
import java.util.List;

/** 9-1 신고 목록 항목 (같은 대상 묶음) */
public record AdminReportItemDTO(
		Long reportId,
		ReportTargetType targetType,
		String targetId,
		ReportSourceType sourceType,
		Long sourceId,
		String sourceTitle,
		Long targetUserId,
		String targetUserName,
		String targetRoomNumber,
		Long buildingId,
		String buildingName,
		String contentPreview,
		List<ReportReason> reasonTypes,
		int reportCount,
		ReportStatus status,
		LocalDateTime latestReportedAt
) {
}
