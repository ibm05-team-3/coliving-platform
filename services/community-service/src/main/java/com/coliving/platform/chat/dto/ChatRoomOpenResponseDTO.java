package com.coliving.platform.chat.dto;

/** 4-2 응답 (created = 새로 만든 방이면 true → 201) */
public record ChatRoomOpenResponseDTO(Long roomId, boolean created) {
}
