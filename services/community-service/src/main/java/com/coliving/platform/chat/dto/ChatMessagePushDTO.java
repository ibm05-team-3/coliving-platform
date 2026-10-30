package com.coliving.platform.chat.dto;

import com.coliving.platform.common.entity.ContentStatus;
import java.time.LocalDateTime;

/** 4-5 실시간 수신 (/user/queue/chat) — 4-3 메시지 항목 + roomId (모든 방 메시지가 한 채널로 옴) */
public record ChatMessagePushDTO(
		Long roomId,
		String messageId,
		Long senderId,
		String content,
		ContentStatus status,
		LocalDateTime sentAt
) {

	public static ChatMessagePushDTO of(Long roomId, ChatMessageDTO message) {
		return new ChatMessagePushDTO(roomId, message.messageId(), message.senderId(), message.content(),
				message.status(), message.sentAt());
	}
}
