package com.coliving.platform.post.dto;

import com.coliving.platform.post.entity.PostCategory;
import java.time.LocalDateTime;

/** 5-1 상단 고정 공지 */
public record PinnedPostDTO(
		Long postId,
		PostCategory category,
		String title,
		String authorName,
		int viewCount,
		int commentCount,
		LocalDateTime createdAt
) {
}
