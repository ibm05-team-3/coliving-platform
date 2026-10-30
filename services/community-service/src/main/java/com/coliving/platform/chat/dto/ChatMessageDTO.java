package com.coliving.platform.chat.dto;

import com.coliving.platform.chat.document.ChatMessageDocument;
import com.coliving.platform.common.entity.ContentStatus;
import java.time.LocalDateTime;

/** 4-3 메시지 · 4-5 실시간 수신 · 4-6 보내기 응답 (숨김·삭제면 content null) */
public record ChatMessageDTO(
		String messageId,
		Long senderId,
		String content,
		ContentStatus status,
		LocalDateTime sentAt
) {

	public static ChatMessageDTO from(ChatMessageDocument message) {
		return new ChatMessageDTO(message.getId(), message.getSenderId(),
				message.isActive() ? message.getContent() : null, message.getStatus(), message.getSentAt());
	}
}
