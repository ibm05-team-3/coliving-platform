package com.coliving.platform.chat.event;

/** 채팅방 나가기 완료 → 커밋 후 남은 사람에게 알림 (partnerId 가 null 이면 이미 둘 다 나감) */
public record ChatRoomLeftEvent(Long roomId, Long leftUserId, Long partnerId) {
}
