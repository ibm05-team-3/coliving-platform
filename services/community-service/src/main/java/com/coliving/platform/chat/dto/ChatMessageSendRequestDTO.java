package com.coliving.platform.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 4-5 WebSocket SEND · 4-6 REST 보내기 */
public record ChatMessageSendRequestDTO(
		@NotBlank(message = "메시지를 입력해 주세요.")
		@Size(max = 1000, message = "메시지는 1,000자 이하로 입력해 주세요.")
		String content
) {
}
