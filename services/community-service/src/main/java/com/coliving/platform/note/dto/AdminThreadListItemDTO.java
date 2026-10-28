package com.coliving.platform.note.dto;

import com.coliving.platform.note.entity.NoteThreadStatus;
import com.coliving.platform.note.entity.NoteThreadType;
import java.time.OffsetDateTime;

/** 3-1 쪽지함 목록 항목 */
public record AdminThreadListItemDTO(
		Long threadId,
		NoteThreadType type,
		Long userId,
		String name,
		String lastContent,
		OffsetDateTime lastAt,
		long unreadCount,
		NoteThreadStatus status
) {
}
