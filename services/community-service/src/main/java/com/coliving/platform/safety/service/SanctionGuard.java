package com.coliving.platform.safety.service;

import org.springframework.stereotype.Component;

/**
 * 제재 확인 — 이용 제한·정지 중이면 글·댓글·채팅 작성 막기 (403 SANCTIONED)
 * TODO(COL-156): Redis sanction:active:{userId} → user_sanctions 조회로 구현. 지금은 통과
 */
@Component
public class SanctionGuard {

	public void checkCanWrite(Long userId) {
		// COL-156에서 구현
	}
}
