package com.coliving.platform.safety.dto;

import java.util.List;

/** 9-1 신고 목록 응답 */
public record AdminReportListResponseDTO(
		ReportSummaryDTO summary,
		List<AdminReportItemDTO> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
