package com.coliving.platform.safety.service;

import com.coliving.platform.safety.dto.MySanctionDTO;
import com.coliving.platform.safety.entity.SanctionType;
import com.coliving.platform.safety.entity.UserSanctionEntity;
import com.coliving.platform.safety.repository.UserSanctionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 진행 중인 이용 제한·정지 조회 (8-5, 5-9, 글쓰기 막기) */
@Service
@RequiredArgsConstructor
public class SanctionQueryService {

	static final List<SanctionType> BLOCKING_TYPES = List.of(SanctionType.RESTRICTION, SanctionType.SUSPENSION);

	private final UserSanctionRepository userSanctionRepository;

	@Transactional(readOnly = true)
	public Optional<UserSanctionEntity> findActive(Long userId) {
		return userSanctionRepository.findActive(userId, BLOCKING_TYPES, LocalDateTime.now()).stream().findFirst();
	}

	/** 8-5 내 이용 제한 상태 (없으면 active = false) */
	@Transactional(readOnly = true)
	public MySanctionDTO getMySanction(Long userId) {
		return findActive(userId)
				.map(s -> new MySanctionDTO(true, s.getId(), s.getSanctionType(), s.getDurationDays(), s.getReason(),
						s.getStartsAt(), s.getEndsAt()))
				.orElseGet(MySanctionDTO::none);
	}
}
