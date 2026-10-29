package com.coliving.platform.post.dto;

import java.time.LocalDateTime;

/** 5-6 응답 */
public record CommentCreateResponseDTO(
		Long commentId,
		Long parentCommentId,
		LocalDateTime createdAt
) {
}
