package com.coliving.platform.chat.event;

import com.coliving.platform.chat.dto.ChatMessagePushDTO;
import com.coliving.platform.chat.dto.ChatRoomEventDTO;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 채팅 메시지 실시간 전송 (비동기)
 * DB 저장이 커밋된 뒤에만 보냄 → 저장 실패한 메시지가 화면에 뜨는 일 없음
 * 보내기 요청은 전송·알림을 기다리지 않고 바로 응답
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessagePushListener {

	public static final String CHAT_QUEUE = "/queue/chat";
	public static final String CHAT_EVENT_QUEUE = "/queue/chat-events";

	private final SimpMessagingTemplate messagingTemplate;
	private final NotificationService notificationService;
	private final UserClient userClient;

	@Async
	@TransactionalEventListener(fallbackExecution = true)
	public void onMessageSent(ChatMessageSentEvent event) {
		ChatMessagePushDTO payload = ChatMessagePushDTO.of(event.roomId(), event.message());

		// 1) 알림 저장 먼저 — 실시간 전송이 실패해도 알림은 남도록 따로 처리
		if (!event.hidden()) {   // 차단한 사람에게는 알림 없음
			try {
				UserSummary sender = userClient.getUser(event.senderId());
				notificationService.notifyChat(event.receiverId(), event.roomId(),
						sender != null ? sender.name() : "알 수 없음", event.message().content());
			} catch (Exception e) {
				log.warn("채팅 알림 저장 실패 roomId={}, messageId={}", event.roomId(), event.message().messageId(), e);
			}
		}

		// 2) 실시간 전송 — 실패해도 메시지는 저장돼 있어서 다음에 4-3 으로 불러오면 보임
		try {
			// 보낸 사람의 다른 기기·탭에도 보이도록 보낸 사람에게도 전송
			messagingTemplate.convertAndSendToUser(String.valueOf(event.senderId()), CHAT_QUEUE, payload);
			if (!event.hidden()) {   // 차단한 사람에게는 전송 없음
				messagingTemplate.convertAndSendToUser(String.valueOf(event.receiverId()), CHAT_QUEUE, payload);
			}
		} catch (Exception e) {
			log.warn("채팅 실시간 전송 실패 roomId={}, messageId={}", event.roomId(), event.message().messageId(), e);
		}
	}

	/** 상대가 나감 → 남은 사람 화면에 "상대방이 대화방을 나갔습니다" + 입력창 막기 */
	@Async
	@TransactionalEventListener(fallbackExecution = true)
	public void onRoomLeft(ChatRoomLeftEvent event) {
		if (event.partnerId() == null) {
			return;
		}
		try {
			messagingTemplate.convertAndSendToUser(String.valueOf(event.partnerId()), CHAT_EVENT_QUEUE,
					ChatRoomEventDTO.partnerLeft(event.roomId(), event.leftUserId()));
		} catch (Exception e) {
			log.warn("채팅방 나가기 실시간 전송 실패 roomId={}", event.roomId(), e);
		}
	}
}
