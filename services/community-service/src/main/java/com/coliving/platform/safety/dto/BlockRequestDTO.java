package com.coliving.platform.safety.dto;

import jakarta.validation.constraints.NotNull;

/** 8-3 차단하기 */
public record BlockRequestDTO(
		@NotNull(message = "차단할 사용자를 선택해 주세요.")
		Long blockedUserId
) {
}
