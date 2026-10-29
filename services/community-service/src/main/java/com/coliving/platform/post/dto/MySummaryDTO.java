package com.coliving.platform.post.dto;

import java.util.List;

/** 5-9 내 커뮤니티 정보 · 인기글 */
public record MySummaryDTO(
		String name,
		String buildingName,
		String roomNumber,
		long postCount,
		long receivedCommentCount,
		/** 진행 중인 이용 제한 (TODO COL-156: 8-5 응답 구조로 채우기) */
		Object sanction,
		List<PopularPostDTO> popularPosts
) {
}
