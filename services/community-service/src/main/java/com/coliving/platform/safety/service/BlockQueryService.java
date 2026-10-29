package com.coliving.platform.safety.service;

import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 차단 조회 — 게시판·채팅에서 "내가 차단한 사람" 확인용
 * TODO(COL-156): user_blocks 테이블 조회로 구현. 지금은 차단 없음으로 처리
 */
@Service
public class BlockQueryService {

	/** 내가 차단한 사용자 ID 목록 */
	public Set<Long> getBlockedUserIds(Long userId) {
		return Set.of();
	}

	/** target 이 me 를 차단했는지 (알림 생략·채팅 막기용) */
	public boolean isBlockedBy(Long me, Long target) {
		return false;
	}
}
