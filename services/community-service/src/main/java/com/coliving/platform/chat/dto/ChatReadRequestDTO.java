package com.coliving.platform.chat.dto;

import jakarta.validation.constraints.NotBlank;

/** 4-4 읽음 처리 */
public record ChatReadRequestDTO(
		@NotBlank(message = "마지막으로 읽은 메시지 ID가 없습니다.")
		String lastReadMessageId
) {
}
