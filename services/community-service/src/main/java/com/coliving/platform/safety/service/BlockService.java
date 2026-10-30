package com.coliving.platform.safety.service;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.safety.dto.BlockListResponseDTO;
import com.coliving.platform.safety.dto.BlockResponseDTO;
import com.coliving.platform.safety.dto.BlockedUserDTO;
import com.coliving.platform.safety.entity.UserBlockEntity;
import com.coliving.platform.safety.repository.UserBlockRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 차단 관리 (API 8-2 ~ 8-4) */
@Service
@RequiredArgsConstructor
public class BlockService {

	private final UserBlockRepository userBlockRepository;
	private final ResidentGuard residentGuard;
	private final UserClient userClient;
	private final ContractClient contractClient;

	/** 8-2 차단 목록 (최근 차단 순) */
	@Transactional(readOnly = true)
	public BlockListResponseDTO getBlocks(Long userId) {
		residentGuard.requireResident(userId);
		List<UserBlockEntity> blocks = userBlockRepository.findByBlockerIdOrderByCreatedAtDesc(userId);
		List<Long> ids = blocks.stream().map(UserBlockEntity::getBlockedId).toList();
		Map<Long, UserSummary> users = ids.isEmpty() ? Map.of() : userClient.getUsers(ids);
		Map<Long, ResidentRoom> rooms = ids.isEmpty() ? Map.of() : contractClient.getResidentRooms(ids);

		List<BlockedUserDTO> result = blocks.stream()
				.map(block -> {
					UserSummary user = users.get(block.getBlockedId());
					ResidentRoom room = rooms.get(block.getBlockedId());
					return new BlockedUserDTO(
							block.getBlockedId(),
							user != null ? user.name() : "알 수 없는 사용자",
							room != null ? room.roomNumber() : null,
							user != null ? user.profileImageUrl() : null,
							block.getCreatedAt());
				})
				.toList();
		return new BlockListResponseDTO(result);
	}

	/** 8-3 차단하기 (본인 403, 이미 차단 409, 없는 회원 404) */
	@Transactional
	public BlockResponseDTO block(Long userId, Long blockedUserId) {
		residentGuard.requireResident(userId);
		if (userId.equals(blockedUserId)) {
			throw new CommunityException(ErrorCode.CANNOT_BLOCK_SELF);
		}
		if (userClient.getUser(blockedUserId) == null) {
			throw new CommunityException(ErrorCode.USER_NOT_FOUND);
		}
		if (userBlockRepository.existsByBlockerIdAndBlockedId(userId, blockedUserId)) {
			throw new CommunityException(ErrorCode.ALREADY_BLOCKED);
		}
		UserBlockEntity saved = userBlockRepository.save(UserBlockEntity.create(userId, blockedUserId));
		return new BlockResponseDTO(blockedUserId, saved.getCreatedAt());
	}

	/** 8-4 차단 해제 (내역 없으면 404) */
	@Transactional
	public void unblock(Long userId, Long blockedUserId) {
		UserBlockEntity block = userBlockRepository.findByBlockerIdAndBlockedId(userId, blockedUserId)
				.orElseThrow(() -> new CommunityException(ErrorCode.BLOCK_NOT_FOUND));
		userBlockRepository.delete(block);
	}
}
