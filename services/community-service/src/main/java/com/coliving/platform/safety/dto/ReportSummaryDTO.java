package com.coliving.platform.safety.dto;

/** 9-1 상태별 신고 대상 수 (receivedCount = 사이드바 배지) */
public record ReportSummaryDTO(
		long receivedCount,
		long resolvedCount,
		long rejectedCount
) {
}
