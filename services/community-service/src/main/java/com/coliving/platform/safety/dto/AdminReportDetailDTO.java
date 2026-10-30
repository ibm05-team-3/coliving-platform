package com.coliving.platform.safety.dto;

import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.safety.entity.ReportAction;
import com.coliving.platform.safety.entity.ReportReason;
import com.coliving.platform.safety.entity.ReportSourceType;
import com.coliving.platform.safety.entity.ReportStatus;
import com.coliving.platform.safety.entity.ReportTargetType;
import com.coliving.platform.safety.entity.SanctionType;
import java.time.LocalDateTime;
import java.util.List;

/** 9-2 신고 상세 */
public record AdminReportDetailDTO(
		TargetInfo target,
		TargetUserInfo targetUser,
		List<ReportInfo> reports,
		HistoryInfo history,
		ReportStatus status,
		ReportAction action,
		String adminMemo,
		String handledByName,
		LocalDateTime handledAt
) {

	public record TargetInfo(ReportTargetType targetType, String targetId, ReportSourceType sourceType, Long sourceId,
			String sourceTitle, ReportSnapshot snapshot, ContentStatus currentStatus) {
	}

	public record TargetUserInfo(Long userId, String name, String roomNumber, String buildingName) {
	}

	/** 신고자 정보는 넣지 않음 (익명) */
	public record ReportInfo(Long reportId, ReportReason reasonType, String reasonDetail, LocalDateTime createdAt) {
	}

	public record HistoryInfo(long reportedCount, long warningCount, List<SanctionHistoryInfo> sanctions) {
	}

	public record SanctionHistoryInfo(Long sanctionId, SanctionType sanctionType, Integer durationDays, String reason,
			LocalDateTime startsAt, LocalDateTime endsAt, LocalDateTime revokedAt) {
	}
}
