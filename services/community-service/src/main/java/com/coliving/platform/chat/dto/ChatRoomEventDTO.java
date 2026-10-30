package com.coliving.platform.chat.dto;

/** 4-5 채팅방 이벤트 (/user/queue/chat-events) — type: PARTNER_LEFT (상대가 나감) */
public record ChatRoomEventDTO(String type, Long roomId, Long userId) {

	public static ChatRoomEventDTO partnerLeft(Long roomId, Long userId) {
		return new ChatRoomEventDTO("PARTNER_LEFT", roomId, userId);
	}
}
