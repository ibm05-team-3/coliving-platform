package com.coliving.platform.chat.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 입주자 1:1 채팅방 (메시지는 MongoDB chat_messages) */
@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long buildingId;

	/** '작은ID_큰ID' — 같은 두 사람의 방은 하나만 */
	@Column(nullable = false, length = 50)
	private String pairKey;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "chat_source_type")
	private ChatSourceType sourceType;

	private Long sourcePostId;

	@Column(nullable = false)
	private boolean readOnly;

	@Column(length = 100)
	private String lastMessagePreview;

	private LocalDateTime lastMessageAt;

	/** 종료 일시 — 누군가 나가면 종료 (다시 시작하면 새 방). 종료된 방은 pair_key 뒤에 #방ID 를 붙여 비워 둠 */
	private LocalDateTime closedAt;

	public static ChatRoomEntity create(Long buildingId, Long userA, Long userB, ChatSourceType sourceType,
			Long sourcePostId) {
		ChatRoomEntity room = new ChatRoomEntity();
		room.buildingId = buildingId;
		room.pairKey = pairKey(userA, userB);
		room.sourceType = sourceType;
		room.sourcePostId = sourceType == ChatSourceType.POST ? sourcePostId : null;
		return room;
	}

	public static String pairKey(Long userA, Long userB) {
		return Math.min(userA, userB) + "_" + Math.max(userA, userB);
	}

	/** 숨김 메시지가 아닐 때만 호출 (4-1 목록은 보는 사람 기준으로 MongoDB에서 다시 계산) */
	public void updateLastMessage(String content, LocalDateTime sentAt) {
		this.lastMessagePreview = preview(content);
		this.lastMessageAt = sentAt;
	}

	/** 미리보기 100자 (DB 컬럼 길이) */
	public static String preview(String content) {
		return content.length() <= 100 ? content : content.substring(0, 99) + "…";
	}

	/** 한 명이라도 나가면 종료 — 같은 두 사람이 다시 채팅하면 새 방이 만들어지도록 pair_key 를 바꿔 둠 */
	public void close() {
		if (closedAt == null) {
			this.closedAt = LocalDateTime.now();
			this.pairKey = pairKey + "#" + id;
		}
	}

	public boolean isClosed() {
		return closedAt != null;
	}

	/** 퇴거·탈퇴 시 읽기 전용 (Kafka 이벤트에서 호출 예정) */
	public void makeReadOnly() {
		this.readOnly = true;
	}
}
