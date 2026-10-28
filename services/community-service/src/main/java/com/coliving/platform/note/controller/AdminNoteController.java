package com.coliving.platform.note.controller;

import com.coliving.platform.common.dto.PageResponse;
import com.coliving.platform.note.dto.AdminNoteCreateRequestDTO;
import com.coliving.platform.note.dto.AdminNoteCreateResponseDTO;
import com.coliving.platform.note.dto.AdminReplyResponseDTO;
import com.coliving.platform.note.dto.AdminThreadDetailDTO;
import com.coliving.platform.note.dto.AdminThreadListItemDTO;
import com.coliving.platform.note.dto.AdminThreadUpdateRequestDTO;
import com.coliving.platform.note.dto.AdminThreadUpdateResponseDTO;
import com.coliving.platform.note.dto.NoteCreateRequestDTO;
import com.coliving.platform.note.entity.NoteThreadStatus;
import com.coliving.platform.note.entity.NoteThreadType;
import com.coliving.platform.note.service.AdminNoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 쪽지함 — 관리자 (API 3-1 ~ 3-5)
 * TODO: 관리자 권한 확인 (Gateway가 넘겨주는 역할 정보 확정 후)
 */
@RestController
@RequestMapping("/community-service/admin")
@RequiredArgsConstructor
public class AdminNoteController {

	private final AdminNoteService adminNoteService;

	@GetMapping("/note-threads")
	public PageResponse<AdminThreadListItemDTO> getThreads(
			@RequestParam(required = false) NoteThreadType type,
			@RequestParam(required = false) NoteThreadStatus status,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return adminNoteService.getThreads(type, status, keyword, page, size);
	}

	@GetMapping("/note-threads/{threadId}")
	public AdminThreadDetailDTO getThread(@PathVariable Long threadId) {
		return adminNoteService.getThread(threadId);
	}

	@PostMapping("/note-threads/{threadId}/notes")
	public ResponseEntity<AdminReplyResponseDTO> reply(
			@RequestHeader("X-User") Long adminId,
			@PathVariable Long threadId,
			@Valid @RequestBody NoteCreateRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminNoteService.reply(adminId, threadId, request.content()));
	}

	@PatchMapping("/note-threads/{threadId}")
	public AdminThreadUpdateResponseDTO update(
			@PathVariable Long threadId,
			@Valid @RequestBody AdminThreadUpdateRequestDTO request) {
		return adminNoteService.update(threadId, request);
	}

	@PostMapping("/notes")
	public ResponseEntity<AdminNoteCreateResponseDTO> create(
			@RequestHeader("X-User") Long adminId,
			@Valid @RequestBody AdminNoteCreateRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminNoteService.create(adminId, request));
	}
}
