package com.coliving.platform.note.dto;

import java.time.LocalDateTime;

/** 3-5 응답 */
public record AdminNoteCreateResponseDTO(Long threadId, Long noteId, LocalDateTime createdAt) {
}
