package com.coliving.platform.chat.event;

import com.coliving.platform.chat.dto.ChatMessageDTO;

/**
 * 메시지 저장 완료 → 커밋 후 실시간 전송·알림 (ChatMessagePushListener)
 * hidden = 받는 사람이 보낸 사람을 차단한 상태 → 보낸 사람에게만 전송, 알림 없음
 */
public record ChatMessageSentEvent(Long roomId, Long senderId, Long receiverId, ChatMessageDTO message, boolean hidden) {
}
