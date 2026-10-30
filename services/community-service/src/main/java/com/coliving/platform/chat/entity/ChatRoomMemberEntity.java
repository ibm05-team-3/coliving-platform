package com.coliving.platform.chat.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 채팅방 참여자 — 읽음 위치(MongoDB 메시지 ID) 저장 */
@Entity
@Table(name = "chat_room_members")
@IdClass(ChatRoomMemberId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMemberEntity extends BaseTimeEntity {

	@Id
	@Column(name = "chat_room_id")
	private Long chatRoomId;

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Column(length = 30)
	private String lastReadMessageId;

	/** 나간 일시 — 나가면 내 목록에서 숨김, 다시 들어올 수 없음 (상대는 방이 남아서 신고 가능) */
	private LocalDateTime leftAt;

	public static ChatRoomMemberEntity create(Long chatRoomId, Long userId) {
		ChatRoomMemberEntity member = new ChatRoomMemberEntity();
		member.chatRoomId = chatRoomId;
		member.userId = userId;
		return member;
	}

	public void leave() {
		if (leftAt == null) {
			this.leftAt = LocalDateTime.now();
		}
	}

	public boolean hasLeft() {
		return leftAt != null;
	}

	/** 더 최근 메시지일 때만 읽음 위치 이동 (ObjectId는 시간순이라 문자열 비교 가능) */
	public void readUpTo(String messageId) {
		if (lastReadMessageId == null || messageId.compareTo(lastReadMessageId) > 0) {
			this.lastReadMessageId = messageId;
		}
	}
}
