package com.coliving.platform.note.service;

import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.crypto.CryptoService;
import com.coliving.platform.common.dto.PageResponse;
import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.common.time.KstTime;
import com.coliving.platform.note.dto.AdminNoteCreateRequestDTO;
import com.coliving.platform.note.dto.AdminNoteCreateResponseDTO;
import com.coliving.platform.note.dto.AdminReplyResponseDTO;
import com.coliving.platform.note.dto.AdminThreadDetailDTO;
import com.coliving.platform.note.dto.AdminThreadDetailDTO.GuestInfo;
import com.coliving.platform.note.dto.AdminThreadDetailDTO.NoteInfo;
import com.coliving.platform.note.dto.AdminThreadDetailDTO.UserInfo;
import com.coliving.platform.note.dto.AdminThreadListItemDTO;
import com.coliving.platform.note.dto.AdminThreadUpdateRequestDTO;
import com.coliving.platform.note.dto.AdminThreadUpdateResponseDTO;
import com.coliving.platform.note.entity.GuestInquiryEntity;
import com.coliving.platform.note.entity.NoteEntity;
import com.coliving.platform.note.entity.NoteThreadEntity;
import com.coliving.platform.note.entity.NoteThreadStatus;
import com.coliving.platform.note.entity.NoteThreadType;
import com.coliving.platform.note.repository.NoteRepository;
import com.coliving.platform.note.repository.NoteThreadRepository;
import com.coliving.platform.note.repository.NoteThreadSpecs;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 쪽지함 — 관리자 (API 3장)
 * TODO: 관리자 담당 건물(연동 1-3) 연동 후 담당 건물 쪽지함만 조회하도록 제한
 */
@Service
@RequiredArgsConstructor
public class AdminNoteService {

	private final NoteThreadRepository noteThreadRepository;
	private final NoteRepository noteRepository;
	private final NotificationService notificationService;
	private final CryptoService cryptoService;
	private final UserClient userClient;
	private final ContractClient contractClient;

	/** 3-1 쪽지함 목록 */
	@Transactional(readOnly = true)
	public PageResponse<AdminThreadListItemDTO> getThreads(NoteThreadType type, NoteThreadStatus status,
			String keyword, int page, int size) {
		List<Long> matchedUserIds = (keyword == null || keyword.isBlank())
				? List.of()
				: userClient.searchUsers(keyword.trim()).stream().map(UserSummary::userId).toList();

		Page<NoteThreadEntity> threads = noteThreadRepository.findAll(
				NoteThreadSpecs.search(type, status, keyword, matchedUserIds),
				PageRequest.of(page, size, Sort.by(Sort.Order.desc("lastNoteAt").nullsLast(), Sort.Order.desc("id"))));

		List<Long> userIds = threads.getContent().stream()
				.map(NoteThreadEntity::getUserId)
				.filter(id -> id != null)
				.toList();
		Map<Long, UserSummary> users = userIds.isEmpty() ? Map.of() : userClient.getUsers(userIds);

		List<AdminThreadListItemDTO> content = threads.getContent().stream()
				.map(thread -> {
					NoteEntity last = noteRepository.findFirstByNoteThread_IdOrderByCreatedAtDesc(thread.getId()).orElse(null);
					return new AdminThreadListItemDTO(
							thread.getId(),
							thread.getThreadType(),
							thread.getUserId(),
							displayName(thread, users),
							last != null ? last.getContent() : null,
							KstTime.of(thread.getLastNoteAt()),
							noteRepository.countByNoteThread_IdAndSenderTypeAndReadAtIsNull(thread.getId(), SenderType.USER),
							thread.getStatus());
				})
				.toList();
		return PageResponse.of(threads, content);
	}

	/** 3-2 쪽지함 상세 (사용자가 보낸 쪽지 읽음 처리) */
	@Transactional
	public AdminThreadDetailDTO getThread(Long threadId) {
		NoteThreadEntity thread = findThread(threadId);
		noteRepository.markAllRead(threadId, SenderType.USER, KstTime.now());

		UserInfo user = null;
		GuestInfo guest = null;
		if (thread.isGuest()) {
			GuestInquiryEntity inquiry = thread.getGuestInquiry();
			guest = new GuestInfo(
					inquiry.getName(),
					cryptoService.decrypt(inquiry.getPhoneEnc()),
					cryptoService.decrypt(inquiry.getEmailEnc()),
					KstTime.of(inquiry.getPrivacyAgreedAt()));
		} else {
			UserSummary summary = userClient.getUser(thread.getUserId());
			ResidentRoom room = contractClient.getResidentRooms(List.of(thread.getUserId())).get(thread.getUserId());
			user = new UserInfo(
					thread.getUserId(),
					summary != null ? summary.name() : "알 수 없는 사용자",
					room != null ? room.buildingName() : null,
					room != null ? room.roomNumber() : null);
		}

		List<NoteInfo> notes = noteRepository.findByNoteThread_IdOrderByCreatedAtAsc(threadId).stream()
				.map(note -> new NoteInfo(note.getId(), note.getSenderType(), note.getContent(),
						KstTime.of(note.getCreatedAt()), KstTime.of(note.getReadAt())))
				.toList();
		return new AdminThreadDetailDTO(thread.getId(), thread.getThreadType(), thread.getStatus(), thread.getMemo(),
				user, guest, notes);
	}

	/** 3-3 답장 보내기 (비회원 문의는 전화·이메일로 답변 → 400) */
	@Transactional
	public AdminReplyResponseDTO reply(Long adminId, Long threadId, String content) {
		NoteThreadEntity thread = findThread(threadId);
		if (thread.isGuest()) {
			throw new CommunityException(ErrorCode.GUEST_REPLY_NOT_ALLOWED);
		}
		NoteEntity note = noteRepository.save(NoteEntity.create(thread, SenderType.ADMIN, adminId, content));
		thread.sentByAdmin(note.getCreatedAt());
		notifyUser(thread);
		return new AdminReplyResponseDTO(note.getId(), KstTime.of(note.getCreatedAt()), thread.getStatus());
	}

	/** 3-4 처리 상태 · 메모 변경 (보낸 필드만) */
	@Transactional
	public AdminThreadUpdateResponseDTO update(Long threadId, AdminThreadUpdateRequestDTO request) {
		NoteThreadEntity thread = findThread(threadId);
		if (request.status() != null) {
			thread.changeStatus(request.status());
		}
		if (request.memo() != null) {
			thread.changeMemo(request.memo());
		}
		noteThreadRepository.flush();
		return new AdminThreadUpdateResponseDTO(thread.getId(), thread.getStatus(), thread.getMemo(),
				KstTime.of(thread.getUpdatedAt()));
	}

	/** 3-5 관리자가 먼저 쪽지 쓰기 (쪽지함 없으면 생성) */
	@Transactional
	public AdminNoteCreateResponseDTO create(Long adminId, AdminNoteCreateRequestDTO request) {
		UserSummary recipient = userClient.getUser(request.recipientUserId());
		if (recipient == null) {
			throw new CommunityException(ErrorCode.USER_NOT_FOUND);
		}
		NoteThreadEntity thread = noteThreadRepository.findByUserId(recipient.userId())
				.orElseGet(() -> {
					ResidentRoom room = contractClient.getResidentRooms(List.of(recipient.userId())).get(recipient.userId());
					return noteThreadRepository.save(NoteThreadEntity.forMember(recipient.userId(),
							NoteThreadType.fromRole(recipient.role()), room != null ? room.buildingId() : null));
				});

		NoteEntity note = noteRepository.save(NoteEntity.create(thread, SenderType.ADMIN, adminId, request.content()));
		thread.sentByAdmin(note.getCreatedAt());
		notifyUser(thread);
		return new AdminNoteCreateResponseDTO(thread.getId(), note.getId(), KstTime.of(note.getCreatedAt()));
	}

	private NoteThreadEntity findThread(Long threadId) {
		return noteThreadRepository.findById(threadId)
				.orElseThrow(() -> new CommunityException(ErrorCode.THREAD_NOT_FOUND));
	}

	private String displayName(NoteThreadEntity thread, Map<Long, UserSummary> users) {
		if (thread.isGuest()) {
			return thread.getGuestInquiry().getName();
		}
		UserSummary user = users.get(thread.getUserId());
		return user != null ? user.name() : "알 수 없는 사용자";
	}

	private void notifyUser(NoteThreadEntity thread) {
		notificationService.notifyUser(thread.getUserId(), NotificationType.NOTE, "새 쪽지",
				"매니저가 쪽지를 보냈어요", NotificationTarget.NOTE_THREAD, thread.getId());
	}
}
