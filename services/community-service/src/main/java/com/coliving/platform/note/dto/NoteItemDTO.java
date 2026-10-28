package com.coliving.platform.note.dto;

import com.coliving.platform.common.entity.SenderType;
import java.time.LocalDateTime;

/** 1-1 쪽지 항목 */
public record NoteItemDTO(
		Long noteId,
		SenderType senderType,
		String senderName,
		String content,
		LocalDateTime createdAt,
		LocalDateTime readAt
) {
}
