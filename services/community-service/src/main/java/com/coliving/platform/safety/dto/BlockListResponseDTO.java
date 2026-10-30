package com.coliving.platform.safety.dto;

import java.util.List;

/** 8-2 차단 목록 응답 */
public record BlockListResponseDTO(List<BlockedUserDTO> blocks) {
}
