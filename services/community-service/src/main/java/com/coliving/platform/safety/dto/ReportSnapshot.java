package com.coliving.platform.safety.dto;

/** 신고 당시 내용 (reports.content_snapshot JSON) */
public record ReportSnapshot(
		String content,
		String authorName,
		String writtenAt
) {
}
