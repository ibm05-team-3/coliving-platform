package com.coliving.platform.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 3-5 새 쪽지 쓰기 */
public record AdminNoteCreateRequestDTO(
		@NotNull(message = "받는 사람을 선택해 주세요.")
		Long recipientUserId,

		@NotBlank(message = "내용을 입력해 주세요.")
		@Size(max = 1000, message = "1,000자 이하로 입력해 주세요.")
		String content
) {
}
