package com.coliving.platform.post.controller;

import com.coliving.platform.post.dto.CommentUpdateRequestDTO;
import com.coliving.platform.post.dto.CommentUpdateResponseDTO;
import com.coliving.platform.post.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 커뮤니티 게시판 — 댓글 수정 · 삭제 (API 5-7, 5-8) */
@RestController
@RequestMapping("/community-service/comments")
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;

	@PutMapping("/{commentId}")
	public CommentUpdateResponseDTO update(
			@RequestHeader("X-User") Long userId,
			@PathVariable Long commentId,
			@Valid @RequestBody CommentUpdateRequestDTO request) {
		return commentService.update(userId, commentId, request.content());
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> delete(@RequestHeader("X-User") Long userId, @PathVariable Long commentId) {
		commentService.delete(userId, commentId);
		return ResponseEntity.noContent().build();
	}
}
