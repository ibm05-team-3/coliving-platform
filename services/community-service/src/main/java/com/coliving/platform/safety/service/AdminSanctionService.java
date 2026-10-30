package com.coliving.platform.safety.service;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.PropertyClient;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.dto.PageResponse;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import com.coliving.platform.safety.dto.AdminSanctionItemDTO;
import com.coliving.platform.safety.dto.SanctionRevokeResponseDTO;
import com.coliving.platform.safety.entity.SanctionType;
import com.coliving.platform.safety.entity.UserSanctionEntity;
import com.coliving.platform.safety.repository.UserSanctionRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 제재 관리 — 관리자 (API 9-4, 9-5) */
@Service
@RequiredArgsConstructor
public class AdminSanctionService {

	private final UserSanctionRepository userSanctionRepository;
	private final NotificationService notificationService;
	private final UserClient userClient;
	private final ContractClient contractClient;
	private final PropertyClient propertyClient;

	/** 9-4 제재 목록 */
	@Transactional(readOnly = true)
	public PageResponse<AdminSanctionItemDTO> getSanctions(boolean activeOnly, SanctionType sanctionType,
			Long buildingId, String keyword, int page, int size) {
		List<Long> matchedUserIds = null;
		if (keyword != null && !keyword.isBlank()) {
			matchedUserIds = userClient.searchUsers(keyword.trim()).stream().map(UserSummary::userId).toList();
		}
		LocalDateTime now = LocalDateTime.now();
		Page<UserSanctionEntity> result = userSanctionRepository.findAll(
				filter(activeOnly, sanctionType, buildingId, matchedUserIds, now),
				PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));

		Set<Long> ids = new HashSet<>();
		result.getContent().forEach(s -> {
			ids.add(s.getUserId());
			ids.add(s.getCreatedBy());
		});
		Map<Long, UserSummary> users = ids.isEmpty() ? Map.of() : userClient.getUsers(ids);
		Map<Long, ResidentRoom> rooms = ids.isEmpty() ? Map.of() : contractClient.getResidentRooms(ids);

		List<AdminSanctionItemDTO> content = result.getContent().stream()
				.map(s -> {
					UserSummary user = users.get(s.getUserId());
					UserSummary admin = users.get(s.getCreatedBy());
					ResidentRoom room = rooms.get(s.getUserId());
					return new AdminSanctionItemDTO(
							s.getId(),
							s.getUserId(),
							user != null ? user.name() : "알 수 없는 사용자",
							room != null ? room.roomNumber() : null,
							propertyClient.getBuildingName(s.getBuildingId()),
							s.getSanctionType(),
							s.getDurationDays(),
							s.getReason(),
							s.getReportId(),
							s.getStartsAt(),
							s.getEndsAt(),
							s.isActive(now),
							admin != null ? admin.name() : "관리자",
							s.getRevokedAt(),
							s.getRevokeReason());
				})
				.toList();
		return PageResponse.of(result, content);
	}

	/** 9-5 제재 조기 해제 (이미 해제·만료·경고면 409) */
	@Transactional
	public SanctionRevokeResponseDTO revoke(Long adminId, Long sanctionId, String revokeReason) {
		UserSanctionEntity sanction = userSanctionRepository.findById(sanctionId)
				.orElseThrow(() -> new CommunityException(ErrorCode.SANCTION_NOT_FOUND));
		LocalDateTime now = LocalDateTime.now();
		if (!sanction.isActive(now)) {
			throw new CommunityException(ErrorCode.SANCTION_ALREADY_REVOKED);
		}
		sanction.revoke(adminId, revokeReason.trim(), now);
		notificationService.notifyUser(sanction.getUserId(), NotificationType.SANCTION, "커뮤니티 이용 안내",
				"커뮤니티 이용 제한이 해제됐어요", NotificationTarget.SANCTION, sanction.getId());
		return new SanctionRevokeResponseDTO(sanction.getId(), now);
	}

	private Specification<UserSanctionEntity> filter(boolean activeOnly, SanctionType sanctionType, Long buildingId,
			List<Long> matchedUserIds, LocalDateTime now) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (activeOnly) {
				predicates.add(root.get("sanctionType").in(SanctionQueryService.BLOCKING_TYPES));
				predicates.add(cb.isNull(root.get("revokedAt")));
				predicates.add(cb.or(cb.isNull(root.get("endsAt")), cb.greaterThan(root.<LocalDateTime>get("endsAt"), now)));
			}
			if (sanctionType != null) {
				predicates.add(cb.equal(root.get("sanctionType"), sanctionType));
			}
			if (buildingId != null) {
				predicates.add(cb.equal(root.get("buildingId"), buildingId));
			}
			if (matchedUserIds != null) {
				predicates.add(matchedUserIds.isEmpty() ? cb.disjunction() : root.get("userId").in(matchedUserIds));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}
}
