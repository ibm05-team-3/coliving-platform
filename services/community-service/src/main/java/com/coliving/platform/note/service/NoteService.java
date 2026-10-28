package com.coliving.platform.note.service;

import com.coliving.platform.client.ContractClient;
import java.time.LocalDateTime;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.PropertyClient;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.note.dto.NoteCreateResponseDTO;
import com.coliving.platform.note.dto.NoteItemDTO;
import com.coliving.platform.note.dto.NoteListResponseDTO;
import com.coliving.platform.note.entity.NoteEntity;
import com.coliving.platform.note.entity.NoteThreadEntity;
import com.coliving.platform.note.entity.NoteThreadType;
import com.coliving.platform.note.repository.NoteRepository;
import com.coliving.platform.note.repository.NoteThreadRepository;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 쪽지 — 사용자 (API 1장) */
@Service
@RequiredArgsConstructor
public class NoteService {

	private final NoteThreadRepository noteThreadRepository;
	private final NoteRepository noteRepository;
	private final NotificationService notificationService;
	private final UserClient userClient;
	private final ContractClient contractClient;
	private final PropertyClient propertyClient;

	/** 1-1 내 쪽지 목록 (최신순) */
	@Transactional(readOnly = true)
	public NoteListResponseDTO getMyNotes(Long userId, int page, int size) {
		Optional<NoteThreadEntity> found = noteThreadRepository.findByUserId(userId);
		if (found.isEmpty()) {
			return new NoteListResponseDTO(0, List.of(), page, size, 0, 0);
		}
		NoteThreadEntity thread = found.get();
		Page<NoteEntity> notes = noteRepository.findByNoteThread_Id(thread.getId(),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

		UserSummary me = userClient.getUser(userId);
		String myName = me != null ? me.name() : "나";
		String managerName = managerName(thread.getBuildingId());

		List<NoteItemDTO> content = notes.getContent().stream()
				.map(note -> new NoteItemDTO(
						note.getId(),
						note.getSenderType(),
						note.getSenderType() == SenderType.ADMIN ? managerName : myName,
						note.getContent(),
						note.getCreatedAt(),
						note.getReadAt()))
				.toList();
		long unread = noteRepository.countByNoteThread_IdAndSenderTypeAndReadAtIsNull(thread.getId(), SenderType.ADMIN);
		return new NoteListResponseDTO(unread, content, notes.getNumber(), notes.getSize(),
				notes.getTotalElements(), notes.getTotalPages());
	}

	/** 1-2 관리자에게 쪽지 보내기 (쪽지함 없으면 생성) */
	@Transactional
	public NoteCreateResponseDTO send(Long userId, String content) {
		UserSummary me = userClient.getUser(userId);
		if (me == null) {
			throw new CommunityException(ErrorCode.USER_NOT_FOUND);
		}
		NoteThreadEntity thread = noteThreadRepository.findByUserId(userId)
				.orElseGet(() -> noteThreadRepository.save(createThread(me)));

		NoteEntity note = noteRepository.save(NoteEntity.create(thread, SenderType.USER, userId, content));
		thread.receivedFromUser(note.getCreatedAt());

		notificationService.notifyAdmins(NotificationType.NOTE, "새 쪽지",
				me.name() + "님이 쪽지를 보냈어요", NotificationTarget.NOTE_THREAD, thread.getId());
		return new NoteCreateResponseDTO(note.getId(), note.getCreatedAt());
	}

	/** 1-3 받은 쪽지 전체 읽음 */
	@Transactional
	public void readAll(Long userId) {
		noteThreadRepository.findByUserId(userId)
				.ifPresent(thread -> noteRepository.markAllRead(thread.getId(), SenderType.ADMIN, LocalDateTime.now()));
	}

	private NoteThreadEntity createThread(UserSummary user) {
		ResidentRoom room = contractClient.getResidentRooms(List.of(user.userId())).get(user.userId());
		Long buildingId = room != null ? room.buildingId() : null;
		return NoteThreadEntity.forMember(user.userId(), NoteThreadType.fromRole(user.role()), buildingId);
	}

	private String managerName(Long buildingId) {
		String buildingName = propertyClient.getBuildingName(buildingId);
		return (buildingName != null ? buildingName : "룸링크") + " 매니저";
	}
}
