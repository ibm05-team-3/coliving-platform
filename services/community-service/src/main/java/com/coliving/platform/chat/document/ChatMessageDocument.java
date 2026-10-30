package com.coliving.platform.chat.document;

import com.coliving.platform.common.entity.ContentStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB chat_messages — 인덱스 { roomId: 1, _id: -1 } (ObjectId가 시간순이라 _id로 정렬)
 * 인덱스 자동 생성이 꺼져 있어서 mongosh로 직접 만듦 (적용 가이드 2번)
 */
@Document(collection = "chat_messages")
@CompoundIndex(name = "idx_room_id", def = "{'roomId': 1, '_id': -1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessageDocument {

	@Id
	private String id;

	private Long roomId;

	private Long senderId;

	private String content;

	private ContentStatus status;

	private LocalDateTime sentAt;

	/**
	 * 이 사용자에게는 숨김 (보낼 때 상대가 나를 차단한 상태면 상대 ID)
	 * 보낸 사람은 정상 전송된 것으로 보이고, 차단한 사람은 차단을 풀어도 볼 수 없음 (카카오톡 방식)
	 */
	private Long hiddenFor;

	public static ChatMessageDocument create(Long roomId, Long senderId, String content, Long hiddenFor) {
		ChatMessageDocument message = new ChatMessageDocument();
		message.roomId = roomId;
		message.senderId = senderId;
		message.content = content;
		message.status = ContentStatus.ACTIVE;
		message.sentAt = LocalDateTime.now();
		message.hiddenFor = hiddenFor;
		return message;
	}

	public boolean isVisibleTo(Long userId) {
		return hiddenFor == null || !hiddenFor.equals(userId);
	}

	public boolean isActive() {
		return status == ContentStatus.ACTIVE;
	}

	/** 신고 처리 (숨김·삭제) */
	public void changeStatus(ContentStatus status) {
		this.status = status;
	}
}
