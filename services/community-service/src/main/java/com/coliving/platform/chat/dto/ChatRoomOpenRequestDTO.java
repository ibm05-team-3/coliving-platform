package com.coliving.platform.chat.dto;

import com.coliving.platform.chat.entity.ChatSourceType;
import jakarta.validation.constraints.NotNull;

/** 4-2 채팅방 열기 */
public record ChatRoomOpenRequestDTO(
		@NotNull(message = "채팅 상대를 선택해 주세요.")
		Long partnerUserId,

		@NotNull(message = "채팅 시작 위치가 없습니다.")
		ChatSourceType sourceType,

		Long sourcePostId
) {
}
