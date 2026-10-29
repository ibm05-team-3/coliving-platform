package com.coliving.platform.note.dto;

import com.coliving.platform.note.entity.NoteThreadStatus;
import jakarta.validation.constraints.Size;

/** 3-4 처리 상태 · 메모 변경 (보낸 필드만 변경) */
public record AdminThreadUpdateRequestDTO(
		NoteThreadStatus status,
		@Size(max = 1000, message = "메모는 1,000자 이하로 입력해 주세요.")
		String memo
) {
}
