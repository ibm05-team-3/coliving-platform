package com.coliving.platform.safety.service;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.PropertyClient;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import com.coliving.platform.post.entity.CommentEntity;
import com.coliving.platform.post.entity.PostEntity;
import com.coliving.platform.post.repository.CommentRepository;
import com.coliving.platform.post.repository.PostRepository;
import com.coliving.platform.safety.dto.AdminReportDetailDTO;
import com.coliving.platform.safety.dto.AdminReportDetailDTO.HistoryInfo;
import com.coliving.platform.safety.dto.AdminReportDetailDTO.ReportInfo;
import com.coliving.platform.safety.dto.AdminReportDetailDTO.SanctionHistoryInfo;
import com.coliving.platform.safety.dto.AdminReportDetailDTO.TargetInfo;
import com.coliving.platform.safety.dto.AdminReportDetailDTO.TargetUserInfo;
import com.coliving.platform.safety.dto.AdminReportItemDTO;
import com.coliving.platform.safety.dto.AdminReportListResponseDTO;
import com.coliving.platform.safety.dto.ReportActionRequestDTO;
import com.coliving.platform.safety.dto.ReportActionResponseDTO;
import com.coliving.platform.safety.dto.ReportSnapshot;
import com.coliving.platform.safety.dto.ReportSummaryDTO;
import com.coliving.platform.safety.entity.ReportAction;
import com.coliving.platform.safety.entity.ReportEntity;
import com.coliving.platform.safety.entity.ReportReason;
import com.coliving.platform.safety.entity.ReportSourceType;
import com.coliving.platform.safety.entity.ReportStatus;
import com.coliving.platform.safety.entity.ReportTargetType;
import com.coliving.platform.safety.entity.SanctionType;
import com.coliving.platform.safety.entity.UserSanctionEntity;
import com.coliving.platform.safety.repository.ReportRepository;
import com.coliving.platform.safety.repository.UserSanctionRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 관리 — 관리자 (API 9-1 ~ 9-3)
 * TODO: 관리자 담당 건물(연동 1-3) 연동 후 담당 건물 신고만 보이게 제한
 */
@Service
@RequiredArgsConstructor
public class AdminReportService {

	private static final Set<Integer> DURATIONS = Set.of(1, 3, 7, 30);
	private static final Set<ReportAction> SANCTION_ACTIONS =
			Set.of(ReportAction.WARNING, ReportAction.RESTRICTION, ReportAction.SUSPENSION);

	private final ReportRepository reportRepository;
	private final UserSanctionRepository userSanctionRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final NotificationService notificationService;
	private final UserClient userClient;
	private final ContractClient contractClient;
	private final PropertyClient propertyClient;

	// ---------- 9-1 신고 목록 (대상별 묶음) ----------

	@Transactional(readOnly = true)
	public AdminReportListResponseDTO getReports(ReportStatus status, ReportSourceType sourceType,
			ReportTargetType targetType, Long buildingId, String sort, int page, int size) {
		ReportStatus wanted = status != null ? status : ReportStatus.RECEIVED;
		List<ReportEntity> reports = reportRepository.findAll(filter(sourceType, targetType, buildingId));

		// 같은 대상 + 같은 처리 상태끼리 한 줄로 묶기
		Map<GroupKey, List<ReportEntity>> groups = new LinkedHashMap<>();
		for (ReportEntity report : reports) {
			groups.computeIfAbsent(new GroupKey(report.getTargetType(), report.getTargetId(), report.getStatus()),
					key -> new ArrayList<>()).add(report);
		}
		ReportSummaryDTO summary = new ReportSummaryDTO(
				countGroups(groups, ReportStatus.RECEIVED),
				countGroups(groups, ReportStatus.RESOLVED),
				countGroups(groups, ReportStatus.REJECTED));

		Comparator<List<ReportEntity>> byLatest = Comparator.comparing(this::latestAt, Comparator.reverseOrder());
		Comparator<List<ReportEntity>> byCount = Comparator.<List<ReportEntity>>comparingInt(List::size).reversed();
		Comparator<List<ReportEntity>> order = "count".equals(sort) ? byCount.thenComparing(byLatest) : byLatest;
		List<List<ReportEntity>> selected = groups.entrySet().stream()
				.filter(entry -> entry.getKey().status() == wanted)
				.map(Map.Entry::getValue)
				.sorted(order)
				.toList();

		int from = Math.min(page * size, selected.size());
		int to = Math.min(from + size, selected.size());
		List<List<ReportEntity>> pageGroups = selected.subList(from, to);
		List<AdminReportItemDTO> content = toItems(pageGroups);
		int totalPages = (int) Math.ceil((double) selected.size() / size);
		return new AdminReportListResponseDTO(summary, content, page, size, selected.size(), totalPages);
	}

	/** 사이드바 배지 = 처리 대기 신고 대상 수 */
	@Transactional(readOnly = true)
	public long countReceivedTargets() {
		return getReports(ReportStatus.RECEIVED, null, null, null, "latest", 0, 1).summary().receivedCount();
	}

	// ---------- 9-2 신고 상세 ----------

	@Transactional(readOnly = true)
	public AdminReportDetailDTO getReport(Long reportId) {
		ReportEntity report = findReport(reportId);
		List<ReportEntity> sameTarget = reportRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDescIdDesc(
				report.getTargetType(), report.getTargetId());

		TargetInfo target = new TargetInfo(report.getTargetType(), report.getTargetId(), report.getSourceType(),
				report.getSourceId(), sourceTitle(report), SnapshotJson.fromJson(report.getContentSnapshot()),
				currentStatus(report));

		Long targetUserId = report.getTargetUserId();
		UserSummary user = userClient.getUser(targetUserId);
		ResidentRoom room = contractClient.getResidentRooms(List.of(targetUserId)).get(targetUserId);
		TargetUserInfo targetUser = new TargetUserInfo(targetUserId,
				user != null ? user.name() : "알 수 없는 사용자",
				room != null ? room.roomNumber() : null,
				propertyClient.getBuildingName(report.getBuildingId()));

		List<ReportInfo> reportInfos = sameTarget.stream()
				.map(r -> new ReportInfo(r.getId(), r.getReasonType(), r.getReasonDetail(), r.getCreatedAt()))
				.toList();

		List<SanctionHistoryInfo> sanctions = userSanctionRepository.findByUserIdOrderByCreatedAtDescIdDesc(targetUserId)
				.stream()
				.map(s -> new SanctionHistoryInfo(s.getId(), s.getSanctionType(), s.getDurationDays(), s.getReason(),
						s.getStartsAt(), s.getEndsAt(), s.getRevokedAt()))
				.toList();
		HistoryInfo history = new HistoryInfo(
				reportRepository.countByTargetUserId(targetUserId),
				userSanctionRepository.countByUserIdAndSanctionType(targetUserId, SanctionType.WARNING),
				sanctions);

		String handledByName = null;
		if (report.getHandledBy() != null) {
			UserSummary admin = userClient.getUser(report.getHandledBy());
			handledByName = admin != null ? admin.name() : "관리자";
		}
		return new AdminReportDetailDTO(target, targetUser, reportInfos, history, report.getStatus(),
				report.getAction(), report.getAdminMemo(), handledByName, report.getHandledAt());
	}

	// ---------- 9-3 신고 처리 ----------

	@Transactional
	public ReportActionResponseDTO handle(Long adminId, Long reportId, ReportActionRequestDTO request) {
		ReportEntity report = findReport(reportId);
		if (!report.isReceived()) {
			throw new CommunityException(ErrorCode.ALREADY_HANDLED);
		}
		ReportAction action = request.action();
		validate(report, request);

		LocalDateTime now = LocalDateTime.now();
		if (action == ReportAction.DELETE_CONTENT) {
			moderate(report, true);
		} else if (action == ReportAction.HIDE_CONTENT
				|| (SANCTION_ACTIONS.contains(action) && Boolean.TRUE.equals(request.hideContent())
						&& report.getTargetType() != ReportTargetType.USER)) {
			moderate(report, false);
		}

		Long sanctionId = null;
		LocalDateTime endsAt = null;
		if (SANCTION_ACTIONS.contains(action)) {
			SanctionType type = SanctionType.valueOf(action.name());
			UserSanctionEntity sanction = userSanctionRepository.save(UserSanctionEntity.create(
					report.getTargetUserId(), report.getBuildingId(), type, request.durationDays(),
					request.reason().trim(), report.getId(), adminId, now));
			sanctionId = sanction.getId();
			endsAt = sanction.getEndsAt();
			notifySanction(sanction);
		}

		List<ReportEntity> received = reportRepository.findByTargetTypeAndTargetIdAndStatus(
				report.getTargetType(), report.getTargetId(), ReportStatus.RECEIVED);
		received.forEach(r -> r.handle(action, blankToNull(request.adminMemo()), adminId, now));

		return new ReportActionResponseDTO(report.getId(), received.size(), report.getStatus(), action,
				sanctionId, endsAt, now);
	}

	// ---------- 내부 ----------

	private void validate(ReportEntity report, ReportActionRequestDTO request) {
		ReportAction action = request.action();
		boolean contentAction = action == ReportAction.HIDE_CONTENT || action == ReportAction.DELETE_CONTENT;
		if (contentAction && report.getTargetType() == ReportTargetType.USER) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "사용자 신고는 숨김·삭제할 수 없어요.");
		}
		if (action == ReportAction.RESTRICTION
				&& (request.durationDays() == null || !DURATIONS.contains(request.durationDays()))) {
			throw new CommunityException(ErrorCode.INVALID_DURATION);
		}
		if (SANCTION_ACTIONS.contains(action) && (request.reason() == null || request.reason().isBlank())) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "사용자에게 보낼 사유를 입력해 주세요.");
		}
		if (action == ReportAction.OFFLINE_RECORD && (request.adminMemo() == null || request.adminMemo().isBlank())) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "면담·계약 검토 내용을 메모에 기록해 주세요.");
		}
	}

	/** 게시글·댓글 숨김(false) / 삭제(true). 댓글은 정상 상태였을 때만 게시글 댓글 수 -1 */
	private void moderate(ReportEntity report, boolean delete) {
		long id = Long.parseLong(report.getTargetId());
		switch (report.getTargetType()) {
			case POST -> postRepository.findById(id).filter(PostEntity::isActive).ifPresent(post -> {
				if (delete) {
					post.delete();
				} else {
					post.hide();
				}
			});
			case COMMENT -> commentRepository.findById(id).filter(CommentEntity::isActive).ifPresent(comment -> {
				if (delete) {
					comment.delete();
				} else {
					comment.hide();
				}
				postRepository.addCommentCount(comment.getPost().getId(), -1);
			});
			default -> {
				// USER: 콘텐츠 없음 / CHAT_MESSAGE: TODO(COL-33)
			}
		}
	}

	private void notifySanction(UserSanctionEntity sanction) {
		String message = switch (sanction.getSanctionType()) {
			case WARNING -> "커뮤니티 이용 경고를 받았어요. 사유: " + sanction.getReason();
			case RESTRICTION -> sanction.getDurationDays() + "일 동안 글·댓글·채팅 작성이 제한돼요. 사유: " + sanction.getReason();
			case SUSPENSION -> "관리자가 해제할 때까지 커뮤니티 이용이 정지돼요. 사유: " + sanction.getReason();
		};
		notificationService.notifyUser(sanction.getUserId(), NotificationType.SANCTION, "커뮤니티 이용 안내",
				message, NotificationTarget.SANCTION, sanction.getId());
	}

	private Specification<ReportEntity> filter(ReportSourceType sourceType, ReportTargetType targetType, Long buildingId) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (sourceType != null) {
				predicates.add(cb.equal(root.get("sourceType"), sourceType));
			}
			if (targetType != null) {
				predicates.add(cb.equal(root.get("targetType"), targetType));
			}
			if (buildingId != null) {
				predicates.add(cb.equal(root.get("buildingId"), buildingId));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private long countGroups(Map<GroupKey, List<ReportEntity>> groups, ReportStatus status) {
		return groups.keySet().stream().filter(key -> key.status() == status).count();
	}

	private LocalDateTime latestAt(List<ReportEntity> group) {
		return group.stream().map(ReportEntity::getCreatedAt).filter(Objects::nonNull)
				.max(Comparator.naturalOrder()).orElse(LocalDateTime.MIN);
	}

	private List<AdminReportItemDTO> toItems(List<List<ReportEntity>> groups) {
		List<Long> userIds = groups.stream().map(g -> g.get(0).getTargetUserId()).distinct().toList();
		Map<Long, UserSummary> users = userIds.isEmpty() ? Map.of() : userClient.getUsers(userIds);
		Map<Long, ResidentRoom> rooms = userIds.isEmpty() ? Map.of() : contractClient.getResidentRooms(userIds);
		List<Long> postIds = groups.stream()
				.map(g -> g.get(0))
				.filter(r -> r.getSourceType() == ReportSourceType.POST)
				.map(ReportEntity::getSourceId)
				.distinct()
				.toList();
		Map<Long, String> postTitles = postRepository.findAllById(postIds).stream()
				.collect(Collectors.toMap(PostEntity::getId, PostEntity::getTitle));

		List<AdminReportItemDTO> items = new ArrayList<>();
		for (List<ReportEntity> group : groups) {
			ReportEntity latest = group.stream()
					.max(Comparator.comparing(ReportEntity::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
							.thenComparing(ReportEntity::getId))
					.orElseThrow();
			UserSummary user = users.get(latest.getTargetUserId());
			ResidentRoom room = rooms.get(latest.getTargetUserId());
			ReportSnapshot snapshot = SnapshotJson.fromJson(latest.getContentSnapshot());
			List<ReportReason> reasons = group.stream().map(ReportEntity::getReasonType).distinct().toList();
			items.add(new AdminReportItemDTO(
					latest.getId(),
					latest.getTargetType(),
					latest.getTargetId(),
					latest.getSourceType(),
					latest.getSourceId(),
					latest.getSourceType() == ReportSourceType.POST ? postTitles.get(latest.getSourceId()) : null,
					latest.getTargetUserId(),
					user != null ? user.name() : "알 수 없는 사용자",
					room != null ? room.roomNumber() : null,
					latest.getBuildingId(),
					propertyClient.getBuildingName(latest.getBuildingId()),
					preview(snapshot.content()),
					reasons,
					group.size(),
					latest.getStatus(),
					latest.getCreatedAt()));
		}
		return items;
	}

	private String sourceTitle(ReportEntity report) {
		if (report.getSourceType() != ReportSourceType.POST) {
			return null;
		}
		return postRepository.findById(report.getSourceId()).map(PostEntity::getTitle).orElse(null);
	}

	private ContentStatus currentStatus(ReportEntity report) {
		long id;
		try {
			id = Long.parseLong(report.getTargetId());
		} catch (NumberFormatException e) {
			return null;
		}
		return switch (report.getTargetType()) {
			case POST -> postRepository.findById(id).map(PostEntity::getStatus).orElse(null);
			case COMMENT -> commentRepository.findById(id).map(CommentEntity::getStatus).orElse(null);
			default -> null;
		};
	}

	private ReportEntity findReport(Long reportId) {
		return reportRepository.findById(reportId)
				.orElseThrow(() -> new CommunityException(ErrorCode.REPORT_NOT_FOUND));
	}

	private String preview(String content) {
		if (content == null) {
			return null;
		}
		String oneLine = content.replace('\n', ' ');
		return oneLine.length() <= 50 ? oneLine : oneLine.substring(0, 50) + "…";
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private record GroupKey(ReportTargetType targetType, String targetId, ReportStatus status) {
	}
}
