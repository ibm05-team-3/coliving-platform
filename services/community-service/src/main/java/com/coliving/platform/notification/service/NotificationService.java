package com.coliving.platform.notification.service;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.notification.entity.NotificationEntity;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 저장 (목록·읽음 API와 WebSocket 실시간 전송은 알림 기능 단계에서 추가)
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

	/** TODO: 관리자 담당 건물 API(연동 1-3) 연동 후 건물 담당 관리자에게 각각 보내기. 지금은 0 = 관리자 전체 공용 */
	public static final long ALL_ADMINS = 0L;

	private final NotificationRepository notificationRepository;

	@Transactional
	public void notifyUser(Long userId, NotificationType type, String title, String message,
			NotificationTarget targetType, Object targetId) {
		notificationRepository.save(NotificationEntity.create(userId, SenderType.USER, type, title, message,
				targetType, String.valueOf(targetId)));
	}

	@Transactional
	public void notifyAdmins(NotificationType type, String title, String message,
			NotificationTarget targetType, Object targetId) {
		notificationRepository.save(NotificationEntity.create(ALL_ADMINS, SenderType.ADMIN, type, title, message,
				targetType, String.valueOf(targetId)));
	}
}
