package com.coliving.platform.common.websocket;

import java.security.Principal;

/** WebSocket 연결 사용자 — name = 회원 ID (convertAndSendToUser 의 user 값) */
public record StompPrincipal(String name) implements Principal {

	@Override
	public String getName() {
		return name;
	}
}
