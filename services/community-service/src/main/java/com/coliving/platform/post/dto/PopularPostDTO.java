package com.coliving.platform.post.dto;

/** 5-9 인기글 */
public record PopularPostDTO(
		Long postId,
		String title,
		int viewCount,
		int commentCount
) {
}
