package com.coliving.platform.note.dto;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.note.entity.NoteThreadStatus;
import com.coliving.platform.note.entity.NoteThreadType;
import java.time.OffsetDateTime;
import java.util.List;

/** 3-2 쪽지함 상세 */
public record AdminThreadDetailDTO(
		Long threadId,
		NoteThreadType type,
		NoteThreadStatus status,
		String memo,
		UserInfo user,
		GuestInfo guest,
		List<NoteInfo> notes
) {

	public record UserInfo(Long userId, String name, String buildingName, String roomNumber) {
	}

	public record GuestInfo(String name, String phone, String email, OffsetDateTime privacyAgreedAt) {
	}

	public record NoteInfo(Long noteId, SenderType senderType, String content, OffsetDateTime createdAt,
			OffsetDateTime readAt) {
	}
}
