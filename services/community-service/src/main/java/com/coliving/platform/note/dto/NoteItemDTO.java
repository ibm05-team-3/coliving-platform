package com.coliving.platform.note.dto;

import com.coliving.platform.common.entity.SenderType;
import java.time.OffsetDateTime;

/** 1-1 쪽지 항목 */
public record NoteItemDTO(
		Long noteId,
		SenderType senderType,
		String senderName,
		String content,
		OffsetDateTime createdAt,
		OffsetDateTime readAt
) {
}
