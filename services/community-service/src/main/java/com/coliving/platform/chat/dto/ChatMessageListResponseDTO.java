package com.coliving.platform.chat.dto;

import java.util.List;

/** 4-3 메시지 목록 (오래된 것 → 최신 순) */
public record ChatMessageListResponseDTO(List<ChatMessageDTO> messages, boolean hasMore) {
}
