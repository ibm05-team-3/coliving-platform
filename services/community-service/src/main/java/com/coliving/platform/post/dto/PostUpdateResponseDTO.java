package com.coliving.platform.post.dto;

import java.time.LocalDateTime;

/** 5-4 응답 */
public record PostUpdateResponseDTO(
		Long postId,
		LocalDateTime updatedAt
) {
}
