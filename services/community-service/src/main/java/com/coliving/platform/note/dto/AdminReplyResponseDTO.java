package com.coliving.platform.note.dto;

import com.coliving.platform.note.entity.NoteThreadStatus;
import java.time.LocalDateTime;

/** 3-3 응답 */
public record AdminReplyResponseDTO(Long noteId, LocalDateTime createdAt, NoteThreadStatus threadStatus) {
}
