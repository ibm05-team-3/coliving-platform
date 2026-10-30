package com.coliving.platform.common.websocket;

import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP 로그인·구독 권한 확인
 * - CONNECT: 헤더 X-User 로 사용자 확인 (REST 와 같은 방식)
 *   TODO(Gateway 연동): Authorization: Bearer {JWT} 검증으로 교체
 * - SUBSCRIBE: /user/queue/** (내 것만 오는 개인 채널) 허용, /topic/admin/** 는 관리자만, 나머지는 거부
 * - SEND: /app/** 만 허용
 * 거부하면 STOMP ERROR 프레임이 가고 연결이 끊김
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private final UserClient userClient;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}
		StompCommand command = accessor.getCommand();
		if (command == StompCommand.CONNECT) {
			accessor.setUser(new StompPrincipal(String.valueOf(authenticate(accessor).userId())));
		} else if (command == StompCommand.SUBSCRIBE) {
			checkSubscribe(accessor.getUser(), accessor.getDestination());
		} else if (command == StompCommand.SEND) {
			checkSend(accessor.getUser(), accessor.getDestination());
		}
		return message;
	}

	private UserSummary authenticate(StompHeaderAccessor accessor) {
		String header = accessor.getFirstNativeHeader("X-User");
		try {
			UserSummary user = header == null ? null : userClient.getUser(Long.parseLong(header.trim()));
			if (user == null) {
				throw new MessageDeliveryException("로그인이 필요합니다.");
			}
			return user;
		} catch (NumberFormatException e) {
			throw new MessageDeliveryException("로그인이 필요합니다.");
		}
	}

	/** 보내기는 /app/** (서버 컨트롤러) 만 — /topic·/user 로 직접 보내서 남에게 가짜 메시지를 뿌리는 것 막기 */
	private void checkSend(Principal principal, String destination) {
		if (principal == null) {
			throw new MessageDeliveryException("로그인이 필요합니다.");
		}
		if (destination == null || !destination.startsWith("/app/")) {
			throw new MessageDeliveryException("보낼 수 없는 경로입니다: " + destination);
		}
	}

	private void checkSubscribe(Principal principal, String destination) {
		if (principal == null || destination == null) {
			throw new MessageDeliveryException("로그인이 필요합니다.");
		}
		if (destination.startsWith("/user/queue/")) {
			return;
		}
		if (destination.startsWith("/topic/admin/")) {
			UserSummary user = userClient.getUser(Long.parseLong(principal.getName()));
			if (user != null && "ADMIN".equals(user.role())) {
				return;
			}
		}
		throw new MessageDeliveryException("구독할 수 없는 경로입니다: " + destination);
	}
}
