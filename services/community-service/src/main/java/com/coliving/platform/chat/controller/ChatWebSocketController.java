package com.coliving.platform.chat.controller;

import com.coliving.platform.chat.dto.ChatMessageSendRequestDTO;
import com.coliving.platform.chat.service.ChatService;
import com.coliving.platform.common.exception.CommunityException;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * 4-5 WebSocket 메시지 보내기 — SEND /app/chat/{roomId} { "content": "..." }
 * 성공하면 저장 후 /user/queue/chat 으로 실시간 전송 (ChatMessagePushListener)
 * 실패하면 보낸 사람에게만 /user/queue/errors { code, message, roomId }
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

	private final ChatService chatService;

	@MessageMapping("/chat/{roomId}")
	public void send(@DestinationVariable Long roomId, @Valid @Payload ChatMessageSendRequestDTO request,
			Principal principal) {
		chatService.sendMessage(Long.valueOf(principal.getName()), roomId, request.content());
	}

	@MessageExceptionHandler(CommunityException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public Map<String, Object> handleCommunityException(CommunityException e, SimpMessageHeaderAccessor accessor) {
		Map<String, Object> body = error(e.getErrorCode().name(), e.getBody().getDetail(), accessor);
		if (e.getBody().getProperties() != null) {
			e.getBody().getProperties().forEach((key, value) -> {
				if (!"code".equals(key)) {
					body.put(key, value);   // SANCTIONED 의 sanctionType·endsAt 등
				}
			});
		}
		return body;
	}

	@MessageExceptionHandler(MethodArgumentNotValidException.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public Map<String, Object> handleValidation(MethodArgumentNotValidException e, SimpMessageHeaderAccessor accessor) {
		String message = e.getBindingResult() != null && e.getBindingResult().getFieldError() != null
				? e.getBindingResult().getFieldError().getDefaultMessage()
				: "요청 값이 올바르지 않습니다.";
		return error("INVALID_REQUEST", message, accessor);
	}

	@MessageExceptionHandler(Exception.class)
	@SendToUser(destinations = "/queue/errors", broadcast = false)
	public Map<String, Object> handleUnknown(Exception e, SimpMessageHeaderAccessor accessor) {
		log.error("WebSocket 메시지 처리 오류", e);
		return error("INTERNAL_ERROR", "메시지를 보내지 못했습니다. 잠시 후 다시 시도해 주세요.", accessor);
	}

	private Map<String, Object> error(String code, String message, SimpMessageHeaderAccessor accessor) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("code", code);
		body.put("message", message);
		body.put("roomId", roomIdOf(accessor.getDestination()));
		return body;
	}

	/** /app/chat/{roomId} 에서 roomId 꺼내기 (어느 방에서 실패했는지 화면에 표시용) */
	private Long roomIdOf(String destination) {
		if (destination == null) {
			return null;
		}
		try {
			return Long.valueOf(destination.substring(destination.lastIndexOf('/') + 1));
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
