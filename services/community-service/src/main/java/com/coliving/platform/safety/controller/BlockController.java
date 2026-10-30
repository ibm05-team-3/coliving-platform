package com.coliving.platform.safety.controller;

import com.coliving.platform.safety.dto.BlockListResponseDTO;
import com.coliving.platform.safety.dto.BlockRequestDTO;
import com.coliving.platform.safety.dto.BlockResponseDTO;
import com.coliving.platform.safety.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 차단 관리 (API 8-2 ~ 8-4) */
@RestController
@RequestMapping("/community-service/blocks")
@RequiredArgsConstructor
public class BlockController {

	private final BlockService blockService;

	@GetMapping
	public BlockListResponseDTO getBlocks(@RequestHeader("X-User") Long userId) {
		return blockService.getBlocks(userId);
	}

	@PostMapping
	public ResponseEntity<BlockResponseDTO> block(
			@RequestHeader("X-User") Long userId,
			@Valid @RequestBody BlockRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(blockService.block(userId, request.blockedUserId()));
	}

	@DeleteMapping("/{blockedUserId}")
	public ResponseEntity<Void> unblock(@RequestHeader("X-User") Long userId, @PathVariable Long blockedUserId) {
		blockService.unblock(userId, blockedUserId);
		return ResponseEntity.noContent().build();
	}
}
