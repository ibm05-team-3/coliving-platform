package com.coliving.platform.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 5-6 댓글 · 답글 작성 */
public record CommentRequestDTO(
		@NotBlank(message = "내용을 입력해 주세요.")
		@Size(max = 1000, message = "1,000자 이하로 입력해 주세요.")
		String content,

		Long parentCommentId
) {
}
