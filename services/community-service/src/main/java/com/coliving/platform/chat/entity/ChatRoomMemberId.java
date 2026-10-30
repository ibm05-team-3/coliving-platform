package com.coliving.platform.chat.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** chat_room_members 복합 키 (chat_room_id, user_id) */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatRoomMemberId implements Serializable {

	private Long chatRoomId;
	private Long userId;
}
