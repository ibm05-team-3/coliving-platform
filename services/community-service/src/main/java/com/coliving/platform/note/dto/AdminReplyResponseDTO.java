package com.coliving.platform.note.dto;

import com.coliving.platform.note.entity.NoteThreadStatus;
import java.time.OffsetDateTime;

/** 3-3 응답 */
public record AdminReplyResponseDTO(Long noteId, OffsetDateTime createdAt, NoteThreadStatus threadStatus) {
}
