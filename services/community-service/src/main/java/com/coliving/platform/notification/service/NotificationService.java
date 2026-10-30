package com.coliving.platform.notification.service;

import com.coliving.platform.chat.service.ChatUnreadService;
import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.dto.PageResponse;
import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.note.entity.NoteThreadStatus;
import com.coliving.platform.note.repository.NoteRepository;
import com.coliving.platform.note.repository.NoteThreadRepository;
import com.coliving.platform.notification.dto.NotificationItemDTO;
import com.coliving.platform.notification.dto.UnreadSummaryDTO;
import com.coliving.platform.notification.entity.NotificationEntity;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.event.NotificationCreatedEvent;
import com.coliving.platform.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 저장 · 조회 (API 6장)
 * 관리자 알림은 지금 "관리자 전체 공용(0)"으로 저장 → 관리자는 0번 + 본인 ID 알림을 함께 봄
 * 저장하면 커밋 후 WebSocket 으로 실시간 전송 (NotificationPushListener)
 * TODO: 30일 지난 알림 삭제 스케줄러는 추후
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

	/** TODO: 관리자 담당 건물 API(연동 1-3) 연동 후 건물 담당 관리자에게 각각 보내기. 지금은 0 = 관리자 전체 공용 */
	public static final long ALL_ADMINS = 0L;

	/** 알림 내용 미리보기 길이 */
	private static final int PREVIEW_LENGTH = 20;

	private final NotificationRepository notificationRepository;
	private final NoteThreadRepository noteThreadRepository;
	private final NoteRepository noteRepository;
	private final ResidentGuard residentGuard;
	private final ChatUnreadService chatUnreadService;
	private final ApplicationEventPublisher eventPublisher;

	// ---------- 저장 (다른 기능에서 호출) ----------

	@Transactional
	public void notifyUser(Long userId, NotificationType type, String title, String message,
			NotificationTarget targetType, Object targetId) {
		save(NotificationEntity.create(userId, SenderType.USER, type, title, message, targetType,
				String.valueOf(targetId)));
	}

	@Transactional
	public void notifyAdmins(NotificationType type, String title, String message,
			NotificationTarget targetType, Object targetId) {
		save(NotificationEntity.create(ALL_ADMINS, SenderType.ADMIN, type, title, message, targetType,
				String.valueOf(targetId)));
	}

	/**
	 * 채팅 알림 — 방마다 안 읽은 알림은 1개만, 항상 최신 메시지로 (카카오톡처럼 "박준영: 미리보기")
	 * 이 방의 안 읽은 채팅 알림이 있으면 지우고 새로 만듦 → 목록 맨 위로 올라옴. 4-4 읽음 처리하면 같이 읽음
	 */
	@Transactional
	public void notifyChat(Long userId, Long roomId, String senderName, String content) {
		String roomKey = String.valueOf(roomId);
		notificationRepository.deleteUnreadChat(userId, roomKey);
		save(NotificationEntity.create(userId, SenderType.USER, NotificationType.CHAT, "새 메시지",
				senderName + ": " + preview(content), NotificationTarget.CHAT_ROOM, roomKey));
	}

	/** 알림 미리보기 — 한 줄로, 20자 넘으면 자르고 "…" */
	public static String preview(String content) {
		if (content == null) {
			return "";
		}
		String oneLine = content.strip().replaceAll("\\s+", " ");
		return oneLine.length() <= PREVIEW_LENGTH ? oneLine : oneLine.substring(0, PREVIEW_LENGTH) + "…";
	}

	/** 4-4 채팅방 읽음 → 그 방의 채팅 알림 읽음 */
	@Transactional
	public void markChatRoomRead(Long userId, Long roomId) {
		notificationRepository.markChatRoomRead(userId, String.valueOf(roomId));
	}

	/** 저장 + 커밋 후 실시간 전송 이벤트 */
	private void save(NotificationEntity entity) {
		NotificationEntity saved = notificationRepository.save(entity);
		eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getReceiverType(), saved.getReceiverId(),
				toItem(saved)));
	}

	// ---------- 조회 (API 6장) ----------

	/** 6-1 알림 목록 (최신순) */
	@Transactional(readOnly = true)
	public PageResponse<NotificationItemDTO> getNotifications(Long userId, boolean unreadOnly, int page, int size) {
		Receiver receiver = receiverOf(userId);
		Page<NotificationEntity> result = notificationRepository.findForReceiver(
				receiver.type(), receiver.ids(), unreadOnly, PageRequest.of(page, size));
		List<NotificationItemDTO> content = result.getContent().stream()
				.map(this::toItem)
				.toList();
		return PageResponse.of(result, content);
	}

	/** 6-2 안 읽은 수 — 사용자: 안 읽은 받은 쪽지·채팅 / 관리자: 미답변 쪽지함 수 (관리자는 채팅 없음) */
	@Transactional(readOnly = true)
	public UnreadSummaryDTO getUnreadSummary(Long userId) {
		Receiver receiver = receiverOf(userId);
		long note;
		if (receiver.type() == SenderType.ADMIN) {
			note = noteThreadRepository.countByStatus(NoteThreadStatus.UNANSWERED);
		} else {
			note = noteThreadRepository.findByUserId(userId)
					.map(thread -> noteRepository.countByNoteThread_IdAndSenderTypeAndReadAtIsNull(thread.getId(), SenderType.ADMIN))
					.orElse(0L);
		}
		long chat = receiver.type() == SenderType.ADMIN ? 0 : chatUnreadService.countAllUnread(userId);
		long notification = notificationRepository.countUnread(receiver.type(), receiver.ids());
		return new UnreadSummaryDTO(note, chat, notification, note + chat);
	}

	/** 6-3 알림 하나 읽음 (본인 알림만) */
	@Transactional
	public void markRead(Long userId, Long notificationId) {
		Receiver receiver = receiverOf(userId);
		NotificationEntity notification = notificationRepository.findById(notificationId)
				.filter(n -> n.getReceiverType() == receiver.type() && receiver.ids().contains(n.getReceiverId()))
				.orElseThrow(() -> new CommunityException(ErrorCode.NOTIFICATION_NOT_FOUND));
		notification.markRead();
	}

	/** 6-4 모두 읽음 */
	@Transactional
	public void markAllRead(Long userId) {
		Receiver receiver = receiverOf(userId);
		notificationRepository.markAllRead(receiver.type(), receiver.ids());
	}

	private NotificationItemDTO toItem(NotificationEntity n) {
		return new NotificationItemDTO(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getTargetType(),
				n.getTargetId(), n.isRead(), n.getCreatedAt());
	}

	private Receiver receiverOf(Long userId) {
		if (residentGuard.isAdmin(userId)) {
			return new Receiver(SenderType.ADMIN, List.of(ALL_ADMINS, userId));
		}
		return new Receiver(SenderType.USER, List.of(userId));
	}

	private record Receiver(SenderType type, List<Long> ids) {
	}
}
