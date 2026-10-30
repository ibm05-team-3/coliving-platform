package com.coliving.platform.safety.dto;

import com.coliving.platform.safety.entity.ReportReason;
import com.coliving.platform.safety.entity.ReportSourceType;
import com.coliving.platform.safety.entity.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 8-1 신고하기 */
public record ReportCreateRequestDTO(
		@NotNull(message = "신고 위치가 없습니다.")
		ReportSourceType sourceType,

		@NotNull(message = "신고 위치 ID가 없습니다.")
		Long sourceId,

		@NotNull(message = "신고 대상 종류가 없습니다.")
		ReportTargetType targetType,

		@NotBlank(message = "신고 대상 ID가 없습니다.")
		String targetId,

		@NotNull(message = "신고 사유를 선택해 주세요.")
		ReportReason reasonType,

		@Size(max = 500, message = "상세 내용은 500자 이하로 입력해 주세요.")
		String reasonDetail
) {
}
