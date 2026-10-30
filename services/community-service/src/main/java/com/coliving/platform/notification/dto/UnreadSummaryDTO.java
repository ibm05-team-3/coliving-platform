package com.coliving.platform.notification.dto;

/** 6-2 안 읽은 수 (total = note + chat, 위젯 빨간 점 기준) */
public record UnreadSummaryDTO(
		long note,
		long chat,
		long notification,
		long total
) {
}
