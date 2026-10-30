package com.coliving.platform.chat.dto;

import java.time.LocalDateTime;

/** 4-1 채팅방 목록 항목 (partnerLeft = 상대가 나간 방 → readOnly, "상대방이 대화방을 나갔습니다" 표시) */
public record ChatRoomItemDTO(
		Long roomId,
		Partner partner,
		String lastMessage,
		LocalDateTime lastMessageAt,
		long unreadCount,
		boolean readOnly,
		boolean partnerLeft
) {

	public record Partner(Long userId, String name, String profileImageUrl, String roomNumber) {
	}
}
