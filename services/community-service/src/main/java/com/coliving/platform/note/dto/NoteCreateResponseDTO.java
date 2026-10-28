package com.coliving.platform.note.dto;

import java.time.LocalDateTime;

/** 1-2 응답 */
public record NoteCreateResponseDTO(Long noteId, LocalDateTime createdAt) {
}
