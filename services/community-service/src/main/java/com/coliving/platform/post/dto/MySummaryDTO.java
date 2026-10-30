package com.coliving.platform.post.dto;

import com.coliving.platform.safety.dto.MySanctionDTO;
import java.util.List;

/** 5-9 내 커뮤니티 정보 · 인기글 */
public record MySummaryDTO(
		String name,
		String buildingName,
		String roomNumber,
		long postCount,
		long receivedCommentCount,
		/** 진행 중인 이용 제한·정지 (없으면 null) */
		MySanctionDTO sanction,
		List<PopularPostDTO> popularPosts
) {
}
