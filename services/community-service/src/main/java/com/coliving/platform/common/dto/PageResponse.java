package com.coliving.platform.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/** 목록 응답 공통 형식 (API 명세서 0-2) */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {

	public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
		return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
	}
}
