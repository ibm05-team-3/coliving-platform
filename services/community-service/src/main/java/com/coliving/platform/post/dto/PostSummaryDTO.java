package com.coliving.platform.post.dto;

import com.coliving.platform.post.entity.PostCategory;
import java.time.LocalDateTime;

/** 5-1 게시글 목록 항목 */
public record PostSummaryDTO(
		Long postId,
		PostCategory category,
		String title,
		Long authorId,
		String authorName,
		String authorRoomNumber,
		int viewCount,
		int commentCount,
		LocalDateTime createdAt,
		boolean isNew
) {
}
