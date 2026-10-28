package com.coliving.platform.note.dto;

import com.coliving.platform.note.entity.NoteThreadStatus;
import java.time.OffsetDateTime;

/** 3-4 응답 */
public record AdminThreadUpdateResponseDTO(Long threadId, NoteThreadStatus status, String memo,
		OffsetDateTime updatedAt) {
}
