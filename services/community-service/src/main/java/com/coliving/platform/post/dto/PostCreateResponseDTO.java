package com.coliving.platform.post.dto;

import java.time.LocalDateTime;

/** 5-3 응답 */
public record PostCreateResponseDTO(
		Long postId,
		LocalDateTime createdAt
) {
}
