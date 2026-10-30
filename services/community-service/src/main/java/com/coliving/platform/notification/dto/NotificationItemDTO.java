package com.coliving.platform.notification.dto;

import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import java.time.LocalDateTime;

/** 6-1 알림 항목 */
public record NotificationItemDTO(
		Long notificationId,
		NotificationType type,
		String title,
		String message,
		NotificationTarget targetType,
		String targetId,
		boolean read,
		LocalDateTime createdAt
) {
}
