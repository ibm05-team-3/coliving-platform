package com.coliving.platform.safety.service;

import com.coliving.platform.chat.document.ChatMessageDocument;
import com.coliving.platform.chat.entity.ChatRoomMemberId;
import com.coliving.platform.chat.repository.ChatMessageRepository;
import com.coliving.platform.chat.repository.ChatRoomMemberRepository;
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
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;

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
		return switch (request.targetType()) {
			case POST -> {
				long targetId = parseId(request.targetId());
				requireSource(request, ReportSourceType.POST, targetId);
				PostEntity post = activePost(targetId);
				residentGuard.requireSameBuilding(me, post.getBuildingId());
				yield new Target(post.getAuthorId(), snapshot(post.getTitle() + "\n" + post.getContent(),
						post.getAuthorId(), post.getCreatedAt()));
			}
			case COMMENT -> {
				long targetId = parseId(request.targetId());
				CommentEntity comment = commentRepository.findById(targetId)
						.filter(CommentEntity::isActive)
						.orElseThrow(() -> new CommunityException(ErrorCode.COMMENT_NOT_FOUND));
				requireSource(request, ReportSourceType.POST, comment.getPost().getId());
				residentGuard.requireSameBuilding(me, comment.getPost().getBuildingId());
				yield new Target(comment.getAuthorId(), snapshot(comment.getContent(), comment.getAuthorId(),
						comment.getCreatedAt()));
			}
			case USER -> {
				long targetId = parseId(request.targetId());
				if (userClient.getUser(targetId) == null) {
					throw new CommunityException(ErrorCode.USER_NOT_FOUND);
				}
				ResidentRoom room = contractClient.getResidentRooms(List.of(targetId)).get(targetId);
				if (room == null || !room.buildingId().equals(me.buildingId())) {
					throw new CommunityException(ErrorCode.OTHER_BUILDING);
				}
				if (request.sourceType() == ReportSourceType.POST) {
					residentGuard.requireSameBuilding(me, activePost(request.sourceId()).getBuildingId());
				} else {
					// 채팅방에서 신고 → 나와 상대 모두 그 방 참여자여야 함
					requireChatMember(request.sourceId(), me.userId());
					requireChatParticipant(request.sourceId(), targetId);
				}
				yield new Target(targetId, snapshot("사용자 신고", targetId, LocalDateTime.now()));
			}
			case CHAT_MESSAGE -> {
				// sourceId = 채팅방 ID, targetId = 메시지 ID (MongoDB ObjectId)
				if (request.sourceType() != ReportSourceType.CHAT_ROOM) {
					throw new CommunityException(ErrorCode.INVALID_REQUEST, "채팅 메시지는 채팅방에서만 신고할 수 있어요.");
				}
				requireChatMember(request.sourceId(), me.userId());
				ChatMessageDocument message = chatMessageRepository.findById(request.targetId())
						.filter(m -> m.getRoomId().equals(request.sourceId()))
						.filter(ChatMessageDocument::isActive)
						.filter(m -> m.isVisibleTo(me.userId()))
						.orElseThrow(() -> new CommunityException(ErrorCode.MESSAGE_NOT_FOUND));
				yield new Target(message.getSenderId(), snapshot(message.getContent(), message.getSenderId(),
						message.getSentAt()));
			}
		};
	}

	/** 신고하는 사람: 그 방에 참여 중이어야 함 (나간 방은 404) */
	private void requireChatMember(Long roomId, Long userId) {
		chatRoomMemberRepository.findById(new ChatRoomMemberId(roomId, userId))
				.filter(member -> !member.hasLeft())
				.orElseThrow(() -> new CommunityException(ErrorCode.ROOM_NOT_FOUND));
	}

	/** 신고 대상: 그 방 참여자였으면 됨 (먼저 나간 사람도 신고 가능) */
	private void requireChatParticipant(Long roomId, Long userId) {
		if (!chatRoomMemberRepository.existsById(new ChatRoomMemberId(roomId, userId))) {
			throw new CommunityException(ErrorCode.ROOM_NOT_FOUND);
		}
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
