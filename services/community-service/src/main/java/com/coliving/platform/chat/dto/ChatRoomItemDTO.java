package com.coliving.platform.chat.dto;

import java.time.LocalDateTime;

/** 4-1 채팅방 목록 항목 */
public record ChatRoomItemDTO(
		Long roomId,
		Partner partner,
		String lastMessage,
		LocalDateTime lastMessageAt,
		long unreadCount,
		boolean readOnly
) {

	public record Partner(Long userId, String name, String profileImageUrl, String roomNumber) {
	}
}
