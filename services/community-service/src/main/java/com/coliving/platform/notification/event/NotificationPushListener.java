package com.coliving.platform.notification.event;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 실시간 전송 (비동기, 커밋 후)
 * 사용자 → /user/queue/notifications · 관리자 전체 공용(0) → /topic/admin/notifications
 * 접속 중이 아니면 전송은 사라지지만 DB에 저장돼 있어서 6-1 목록·6-2 숫자로 보임
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushListener {

	public static final String USER_QUEUE = "/queue/notifications";
	public static final String ADMIN_TOPIC = "/topic/admin/notifications";

	private final SimpMessagingTemplate messagingTemplate;

	@Async
	@TransactionalEventListener(fallbackExecution = true)
	public void onNotificationCreated(NotificationCreatedEvent event) {
		try {
			if (event.receiverType() == SenderType.ADMIN && event.receiverId() == NotificationService.ALL_ADMINS) {
				messagingTemplate.convertAndSend(ADMIN_TOPIC, event.notification());
			} else {
				messagingTemplate.convertAndSendToUser(String.valueOf(event.receiverId()), USER_QUEUE,
						event.notification());
			}
		} catch (Exception e) {
			log.warn("알림 실시간 전송 실패 notificationId={}", event.notification().notificationId(), e);
		}
	}
}
