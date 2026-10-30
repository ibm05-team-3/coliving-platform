package com.coliving.platform.safety.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 9-5 제재 해제 */
public record SanctionRevokeRequestDTO(
		@NotBlank(message = "해제 사유를 입력해 주세요.")
		@Size(max = 500, message = "해제 사유는 500자 이하로 입력해 주세요.")
		String revokeReason
) {
}
