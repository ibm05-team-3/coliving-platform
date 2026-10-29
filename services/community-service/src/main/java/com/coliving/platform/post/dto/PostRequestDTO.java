package com.coliving.platform.post.dto;

import com.coliving.platform.post.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 5-3 게시글 작성 · 5-4 수정 */
public record PostRequestDTO(
		@NotNull(message = "카테고리를 선택해 주세요.")
		PostCategory category,

		@NotBlank(message = "제목을 입력해 주세요.")
		@Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
		String title,

		@NotBlank(message = "내용을 입력해 주세요.")
		@Size(max = 5000, message = "내용은 5,000자 이하로 입력해 주세요.")
		String content,

		/** 관리자 공지 작성 시에만 필수 (입주자는 무시하고 입주 건물로 저장) */
		Long buildingId
) {
}
