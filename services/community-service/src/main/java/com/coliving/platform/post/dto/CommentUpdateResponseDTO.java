package com.coliving.platform.post.dto;

import java.time.LocalDateTime;

/** 5-7 응답 */
public record CommentUpdateResponseDTO(
		Long commentId,
		LocalDateTime updatedAt
) {
}
