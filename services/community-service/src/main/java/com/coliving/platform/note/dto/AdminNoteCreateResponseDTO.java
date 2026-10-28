package com.coliving.platform.note.dto;

import java.time.OffsetDateTime;

/** 3-5 응답 */
public record AdminNoteCreateResponseDTO(Long threadId, Long noteId, OffsetDateTime createdAt) {
}
