package com.coliving.platform.common.auth;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 입주자 확인 — 커뮤니티 게시판·채팅은 입주자(거주 중·퇴거 예정)만, 같은 건물만
 * 건물은 요청으로 받지 않고 계약 서비스 정보로 정함 (API 명세서 0-1)
 */
@Component
@RequiredArgsConstructor
public class ResidentGuard {

	private static final Set<String> ACTIVE_RESIDENT_STATUSES = Set.of("RESIDING", "MOVE_OUT_SCHEDULED");

	private final UserClient userClient;
	private final ContractClient contractClient;

	/** 입주자가 아니면 403 NOT_RESIDENT */
	public Resident requireResident(Long userId) {
		UserSummary user = userClient.getUser(userId);
		if (user == null || !ACTIVE_RESIDENT_STATUSES.contains(user.residentStatus())) {
			throw new CommunityException(ErrorCode.NOT_RESIDENT);
		}
		ResidentRoom room = contractClient.getResidentRooms(List.of(userId)).get(userId);
		if (room == null) {
			throw new CommunityException(ErrorCode.NOT_RESIDENT);
		}
		return new Resident(userId, user.name(), room.buildingId(), room.buildingName(), room.roomNumber());
	}

	/** 관리자 여부 (TODO: Gateway가 역할 정보를 넘겨주면 그 값으로 교체) */
	public boolean isAdmin(Long userId) {
		UserSummary user = userClient.getUser(userId);
		return user != null && "ADMIN".equals(user.role());
	}

	/** 같은 건물이 아니면 403 OTHER_BUILDING */
	public void requireSameBuilding(Resident resident, Long buildingId) {
		if (!resident.buildingId().equals(buildingId)) {
			throw new CommunityException(ErrorCode.OTHER_BUILDING);
		}
	}

	public record Resident(Long userId, String name, Long buildingId, String buildingName, String roomNumber) {
	}
}
