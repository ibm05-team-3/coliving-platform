package com.coliving.platform.common.config;

import com.coliving.platform.common.websocket.StompAuthChannelInterceptor;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP (API 4-5)
 * 연결 /community-service/ws · 보내기 /app/** · 개인 수신 /user/queue/** · 관리자 공용 /topic/admin/**
 * 메시지 브로커는 서버 메모리(SimpleBroker) — 서버 1대 기준. 여러 대로 늘리면 RabbitMQ STOMP 릴레이로 교체
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

	/** 프론트 주소 (config: community.chat.allowed-origins, 쉼표로 구분) — 같은 주소(8084)에서 여는 테스트 페이지는 항상 허용 */
	@Value("${community.chat.allowed-origins:}")
	private String allowedOrigins;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		String[] origins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toArray(String[]::new);
		registry.addEndpoint("/community-service/ws").setAllowedOriginPatterns(origins);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue", "/topic");
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompAuthChannelInterceptor);
	}
}
