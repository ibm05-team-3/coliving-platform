package com.coliving.platform.post.dto;

import java.util.List;

/** 5-1 게시글 목록 응답 */
public record PostListResponseDTO(
		Long buildingId,
		String buildingName,
		List<PinnedPostDTO> pinned,
		List<PostSummaryDTO> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
