package com.coliving.platform.note.entity;

import com.coliving.platform.common.time.KstTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 쪽지함 — 회원 1명 또는 비회원 문의 1건당 1개 */
@Entity
@Table(name = "note_threads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteThreadEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "note_thread_type")
	private NoteThreadType threadType;

	private Long userId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "guest_inquiry_id")
	private GuestInquiryEntity guestInquiry;

	private Long buildingId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "note_thread_status")
	private NoteThreadStatus status;

	@Column(columnDefinition = "TEXT")
	private String memo;

	private OffsetDateTime lastNoteAt;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	public static NoteThreadEntity forMember(Long userId, NoteThreadType type, Long buildingId) {
		NoteThreadEntity thread = new NoteThreadEntity();
		thread.userId = userId;
		thread.threadType = type;
		thread.buildingId = buildingId;
		thread.status = NoteThreadStatus.UNANSWERED;
		return thread;
	}

	public static NoteThreadEntity forGuest(GuestInquiryEntity inquiry) {
		NoteThreadEntity thread = new NoteThreadEntity();
		thread.guestInquiry = inquiry;
		thread.threadType = NoteThreadType.GUEST;
		thread.status = NoteThreadStatus.UNANSWERED;
		return thread;
	}

	public boolean isGuest() {
		return threadType == NoteThreadType.GUEST;
	}

	/** 사용자가 보낸 쪽지 → 미답변 */
	public void receivedFromUser(OffsetDateTime at) {
		this.status = NoteThreadStatus.UNANSWERED;
		this.lastNoteAt = at;
	}

	/** 관리자가 보낸 쪽지 → 답변 완료 */
	public void sentByAdmin(OffsetDateTime at) {
		this.status = NoteThreadStatus.ANSWERED;
		this.lastNoteAt = at;
	}

	public void changeStatus(NoteThreadStatus status) {
		this.status = status;
	}

	public void changeMemo(String memo) {
		this.memo = memo;
	}

	/** 역할·건물이 바뀐 경우 (예: 후보자 → 입주자, Kafka contract.completed) */
	public void changeType(NoteThreadType type, Long buildingId) {
		this.threadType = type;
		this.buildingId = buildingId;
	}

	@PrePersist
	void onCreate() {
		OffsetDateTime now = KstTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = KstTime.now();
	}
}
