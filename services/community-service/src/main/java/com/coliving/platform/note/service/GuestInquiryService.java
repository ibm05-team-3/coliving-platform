package com.coliving.platform.note.service;

import com.coliving.platform.common.crypto.CryptoService;
import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.note.dto.GuestInquiryRequestDTO;
import com.coliving.platform.note.dto.GuestInquiryResponseDTO;
import com.coliving.platform.note.entity.GuestInquiryEntity;
import com.coliving.platform.note.entity.NoteEntity;
import com.coliving.platform.note.entity.NoteThreadEntity;
import com.coliving.platform.note.repository.GuestInquiryRepository;
import com.coliving.platform.note.repository.NoteRepository;
import com.coliving.platform.note.repository.NoteThreadRepository;
import com.coliving.platform.notification.entity.NotificationTarget;
import com.coliving.platform.notification.entity.NotificationType;
import com.coliving.platform.notification.service.NotificationService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 비회원 문의 (API 2장) */
@Service
@RequiredArgsConstructor
public class GuestInquiryService {

	private static final Duration RESUBMIT_LIMIT = Duration.ofMinutes(10);

	private final GuestInquiryRepository guestInquiryRepository;
	private final NoteThreadRepository noteThreadRepository;
	private final NoteRepository noteRepository;
	private final NotificationService notificationService;
	private final CryptoService cryptoService;
	private final StringRedisTemplate redisTemplate;

	/** 2-1 비회원 문의 접수 → GUEST 쪽지함 생성 */
	@Transactional
	public GuestInquiryResponseDTO create(GuestInquiryRequestDTO request) {
		if (!Boolean.TRUE.equals(request.privacyAgreed())) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "개인정보 수집·이용 동의가 필요합니다.");
		}
		checkResubmit(request.phone());

		GuestInquiryEntity inquiry = guestInquiryRepository.save(GuestInquiryEntity.create(
				request.name().trim(),
				cryptoService.encrypt(request.phone()),
				cryptoService.encrypt(request.email().trim()),
				request.content()));

		NoteThreadEntity thread = noteThreadRepository.save(NoteThreadEntity.forGuest(inquiry));
		NoteEntity note = noteRepository.save(NoteEntity.create(thread, SenderType.USER, null, request.content()));
		thread.receivedFromUser(note.getCreatedAt());

		notificationService.notifyAdmins(NotificationType.NOTE, "새 비회원 문의",
				inquiry.getName() + "님이 문의를 남겼어요", NotificationTarget.NOTE_THREAD, thread.getId());
		return new GuestInquiryResponseDTO(inquiry.getId(), inquiry.getCreatedAt());
	}

	/** 같은 연락처 10분 내 재제출 방지 (Redis ratelimit:guest:{전화번호 해시}) */
	private void checkResubmit(String phone) {
		String key = "ratelimit:guest:" + cryptoService.sha256(phone.replace("-", ""));
		Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", RESUBMIT_LIMIT);
		if (Boolean.FALSE.equals(first)) {
			throw new CommunityException(ErrorCode.TOO_MANY_REQUESTS, "문의는 10분 뒤에 다시 남길 수 있어요.");
		}
	}
}
