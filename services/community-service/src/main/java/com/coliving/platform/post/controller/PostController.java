package com.coliving.platform.post.controller;

import com.coliving.platform.post.dto.CommentCreateResponseDTO;
import com.coliving.platform.post.dto.CommentRequestDTO;
import com.coliving.platform.post.dto.PostCreateResponseDTO;
import com.coliving.platform.post.dto.PostDetailDTO;
import com.coliving.platform.post.dto.PostListResponseDTO;
import com.coliving.platform.post.dto.PostRequestDTO;
import com.coliving.platform.post.dto.PostUpdateResponseDTO;
import com.coliving.platform.post.entity.PostCategory;
import com.coliving.platform.post.service.CommentService;
import com.coliving.platform.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 커뮤니티 게시판 — 게시글 · 댓글 작성 (API 5-1 ~ 5-6) */
@RestController
@RequestMapping("/community-service/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	private final CommentService commentService;

	@GetMapping
	public PostListResponseDTO getPosts(
			@RequestHeader("X-User") Long userId,
			@RequestParam(required = false) PostCategory category,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return postService.getPosts(userId, category, keyword, page, size);
	}

	@GetMapping("/{postId}")
	public PostDetailDTO getPost(@RequestHeader("X-User") Long userId, @PathVariable Long postId) {
		return postService.getPost(userId, postId);
	}

	@PostMapping
	public ResponseEntity<PostCreateResponseDTO> create(
			@RequestHeader("X-User") Long userId,
			@Valid @RequestBody PostRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(userId, request));
	}

	@PutMapping("/{postId}")
	public PostUpdateResponseDTO update(
			@RequestHeader("X-User") Long userId,
			@PathVariable Long postId,
			@Valid @RequestBody PostRequestDTO request) {
		return postService.update(userId, postId, request);
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> delete(@RequestHeader("X-User") Long userId, @PathVariable Long postId) {
		postService.delete(userId, postId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{postId}/comments")
	public ResponseEntity<CommentCreateResponseDTO> createComment(
			@RequestHeader("X-User") Long userId,
			@PathVariable Long postId,
			@Valid @RequestBody CommentRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(userId, postId, request));
	}
}
