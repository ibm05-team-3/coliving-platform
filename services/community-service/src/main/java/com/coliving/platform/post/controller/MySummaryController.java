package com.coliving.platform.post.controller;

import com.coliving.platform.post.dto.MySummaryDTO;
import com.coliving.platform.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** 내 커뮤니티 정보 · 인기글 (API 5-9) */
@RestController
@RequiredArgsConstructor
public class MySummaryController {

	private final PostService postService;

	@GetMapping("/community-service/me/summary")
	public MySummaryDTO getMySummary(@RequestHeader("X-User") Long userId) {
		return postService.getMySummary(userId);
	}
}
