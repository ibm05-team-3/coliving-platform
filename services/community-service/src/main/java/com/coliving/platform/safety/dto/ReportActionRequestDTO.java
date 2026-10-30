package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.ReportAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 9-3 신고 처리 (조치) */
public record ReportActionRequestDTO(
		@NotNull(message = "조치를 선택해 주세요.")
		ReportAction action,

		Integer durationDays,

		Boolean hideContent,

		@Size(max = 500, message = "사유는 500자 이하로 입력해 주세요.")
		String reason,

		String adminMemo
) {
}
