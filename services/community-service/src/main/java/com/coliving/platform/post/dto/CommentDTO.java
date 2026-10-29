package com.coliving.platform.post.dto;

import com.coliving.platform.common.entity.ContentStatus;
import java.time.LocalDateTime;
import java.util.List;

/** 5-2 원댓글 (replies 포함) */
public record CommentDTO(
		Long commentId,
		Long authorId,
		String authorName,
		String authorRoomNumber,
		String content,
		ContentStatus status,
		boolean blocked,
		boolean isMine,
		LocalDateTime createdAt,
		List<ReplyDTO> replies
) {
}
