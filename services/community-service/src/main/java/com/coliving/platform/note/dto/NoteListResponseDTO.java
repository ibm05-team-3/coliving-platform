package com.coliving.platform.note.dto;

import java.util.List;

/** 1-1 내 쪽지 목록 응답 */
public record NoteListResponseDTO(
		long unreadCount,
		List<NoteItemDTO> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
