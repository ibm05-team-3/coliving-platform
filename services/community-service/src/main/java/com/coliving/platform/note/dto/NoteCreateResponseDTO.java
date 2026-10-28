package com.coliving.platform.note.dto;

import java.time.OffsetDateTime;

/** 1-2 응답 */
public record NoteCreateResponseDTO(Long noteId, OffsetDateTime createdAt) {
}
