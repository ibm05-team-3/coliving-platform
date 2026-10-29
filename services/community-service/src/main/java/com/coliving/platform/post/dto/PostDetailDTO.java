package com.coliving.platform.post.dto;

import com.coliving.platform.post.entity.PostCategory;
import java.time.LocalDateTime;
import java.util.List;

/** 5-2 게시글 상세 */
public record PostDetailDTO(
		Long postId,
		PostCategory category,
		String title,
		String content,
		Long authorId,
		String authorName,
		String authorRoomNumber,
		int viewCount,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		boolean isMine,
		List<CommentDTO> comments
) {
}
