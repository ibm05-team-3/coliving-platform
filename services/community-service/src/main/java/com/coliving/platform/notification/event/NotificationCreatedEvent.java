package com.coliving.platform.notification.event;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.notification.dto.NotificationItemDTO;

/** 알림 저장 완료 → 커밋 후 실시간 전송 (NotificationPushListener) */
public record NotificationCreatedEvent(SenderType receiverType, Long receiverId, NotificationItemDTO notification) {
}
