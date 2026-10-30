package com.coliving.platform.safety.service;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.auth.ResidentGuard.Resident;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import com.coliving.platform.post.entity.CommentEntity;
import com.coliving.platform.post.entity.PostEntity;
import com.coliving.platform.post.repository.CommentRepository;
import com.coliving.platform.post.repository.PostRepository;
import com.coliving.platform.safety.dto.ReportCreateRequestDTO;
import com.coliving.platform.safety.dto.ReportCreateResponseDTO;
import com.coliving.platform.safety.dto.ReportSnapshot;
import com.coliving.platform.safety.entity.ReportEntity;
import com.coliving.platform.safety.entity.ReportReason;
import com.coliving.platform.safety.entity.ReportSourceType;
import com.coliving.platform.safety.repository.ReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 신고하기 (API 8-1) — 신고만으로 자동 숨김 없음, 관리자만 처리 */
@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final ResidentGuard residentGuard;
	private final SanctionGuard sanctionGuard;
	private final UserClient userClient;
	private final ContractClient contractClient;
	private final NotificationService notificationService;

	@Transactional
	public ReportCreateResponseDTO create(Long userId, ReportCreateRequestDTO request) {
		Resident me = residentGuard.requireResident(userId);
		sanctionGuard.checkCanWrite(userId);
		if (request.reasonType() == ReportReason.ETC
				&& (request.reasonDetail() == null || request.reasonDetail().isBlank())) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "기타 사유는 상세 내용을 입력해 주세요.");
		}

		Target target = resolveTarget(me, request);
		if (target.userId().equals(userId)) {
			throw new CommunityException(ErrorCode.CANNOT_REPORT_SELF);
		}
		if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(userId, request.targetType(), request.targetId())) {
			throw new CommunityException(ErrorCode.ALREADY_REPORTED);
		}

		ReportEntity saved = reportRepository.save(ReportEntity.create(userId, request.sourceType(), request.sourceId(),
				request.targetType(), request.targetId(), target.userId(), me.buildingId(), request.reasonType(),
				blankToNull(request.reasonDetail()), SnapshotJson.toJson(target.snapshot())));

		notificationService.notifyAdmins(NotificationType.REPORT, "새 신고", "처리할 신고가 접수됐어요",
				NotificationTarget.REPORT, saved.getId());
		return new ReportCreateResponseDTO(saved.getId(), saved.getStatus(), saved.getCreatedAt());
	}

	/** 신고 대상 확인 + 신고 당시 내용 스냅샷 (같은 건물만) */
	private Target resolveTarget(Resident me, ReportCreateRequestDTO request) {
		long targetId = parseId(request.targetId());
		return switch (request.targetType()) {
			case POST -> {
				requireSource(request, ReportSourceType.POST, targetId);
				PostEntity post = activePost(targetId);
				residentGuard.requireSameBuilding(me, post.getBuildingId());
				yield new Target(post.getAuthorId(), snapshot(post.getTitle() + "\n" + post.getContent(),
						post.getAuthorId(), post.getCreatedAt()));
			}
			case COMMENT -> {
				CommentEntity comment = commentRepository.findById(targetId)
						.filter(CommentEntity::isActive)
						.orElseThrow(() -> new CommunityException(ErrorCode.COMMENT_NOT_FOUND));
				requireSource(request, ReportSourceType.POST, comment.getPost().getId());
				residentGuard.requireSameBuilding(me, comment.getPost().getBuildingId());
				yield new Target(comment.getAuthorId(), snapshot(comment.getContent(), comment.getAuthorId(),
						comment.getCreatedAt()));
			}
			case USER -> {
				if (userClient.getUser(targetId) == null) {
					throw new CommunityException(ErrorCode.USER_NOT_FOUND);
				}
				ResidentRoom room = contractClient.getResidentRooms(List.of(targetId)).get(targetId);
				if (room == null || !room.buildingId().equals(me.buildingId())) {
					throw new CommunityException(ErrorCode.OTHER_BUILDING);
				}
				if (request.sourceType() == ReportSourceType.POST) {
					residentGuard.requireSameBuilding(me, activePost(request.sourceId()).getBuildingId());
				}
				// TODO(COL-33): sourceType = CHAT_ROOM 이면 채팅방 참여자인지 확인
				yield new Target(targetId, snapshot("사용자 신고", targetId, LocalDateTime.now()));
			}
			case CHAT_MESSAGE -> throw new CommunityException(ErrorCode.INVALID_REQUEST,
					"채팅 메시지 신고는 채팅 기능(COL-33) 이후 지원돼요.");
		};
	}

	private PostEntity activePost(Long postId) {
		return postRepository.findById(postId)
				.filter(PostEntity::isActive)
				.orElseThrow(() -> new CommunityException(ErrorCode.POST_NOT_FOUND));
	}

	private void requireSource(ReportCreateRequestDTO request, ReportSourceType sourceType, Long sourceId) {
		if (request.sourceType() != sourceType || !request.sourceId().equals(sourceId)) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "신고 위치(sourceType·sourceId)가 대상과 맞지 않아요.");
		}
	}

	private ReportSnapshot snapshot(String content, Long authorId, LocalDateTime writtenAt) {
		UserSummary author = userClient.getUser(authorId);
		return new ReportSnapshot(content, author != null ? author.name() : "알 수 없는 사용자",
				writtenAt != null ? writtenAt.toString() : null);
	}

	private long parseId(String value) {
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "신고 대상 ID가 올바르지 않습니다.");
		}
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private record Target(Long userId, ReportSnapshot snapshot) {
	}
}
