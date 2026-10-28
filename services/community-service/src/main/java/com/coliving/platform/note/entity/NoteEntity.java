package com.coliving.platform.note.entity;

import com.coliving.platform.common.entity.SenderType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 쪽지 (실시간 아님, 알림으로만 알려줌) */
@Entity
@Table(name = "notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "note_thread_id")
	private NoteThreadEntity noteThread;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "sender_type")
	private SenderType senderType;

	/** 회원 또는 관리자 ID (비회원 문의 원문은 null) */
	private Long senderId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	private OffsetDateTime readAt;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	public static NoteEntity create(NoteThreadEntity thread, SenderType senderType, Long senderId, String content) {
		NoteEntity note = new NoteEntity();
		note.noteThread = thread;
		note.senderType = senderType;
		note.senderId = senderId;
		note.content = content;
		note.createdAt = KstTime.now();
		return note;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = KstTime.now();
		}
	}
}
