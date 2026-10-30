package com.coliving.platform.safety.service;

import com.coliving.platform.safety.repository.UserBlockRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 차단 조회 — 게시판·채팅에서 "내가 차단한 사람" / "나를 차단한 사람" 확인 */
@Service
@RequiredArgsConstructor
public class BlockQueryService {

	private final UserBlockRepository userBlockRepository;

	/** 내가 차단한 사용자 ID 목록 */
	@Transactional(readOnly = true)
	public Set<Long> getBlockedUserIds(Long userId) {
		return new HashSet<>(userBlockRepository.findBlockedIds(userId));
	}

	/** target 이 me 를 차단했는지 (알림 생략·채팅 막기용) */
	@Transactional(readOnly = true)
	public boolean isBlockedBy(Long me, Long target) {
		return userBlockRepository.existsByBlockerIdAndBlockedId(target, me);
	}
}
