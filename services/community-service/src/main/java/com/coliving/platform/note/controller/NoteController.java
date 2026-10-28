package com.coliving.platform.note.controller;

import com.coliving.platform.note.dto.NoteCreateRequestDTO;
import com.coliving.platform.note.dto.NoteCreateResponseDTO;
import com.coliving.platform.note.dto.NoteListResponseDTO;
import com.coliving.platform.note.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 쪽지 — 사용자 (API 1-1 ~ 1-3) */
@RestController
@RequestMapping("/community-service/notes")
@RequiredArgsConstructor
public class NoteController {

	private final NoteService noteService;

	@GetMapping
	public NoteListResponseDTO getMyNotes(
			@RequestHeader("X-User") Long userId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "30") @Min(1) @Max(100) int size) {
		return noteService.getMyNotes(userId, page, size);
	}

	@PostMapping
	public ResponseEntity<NoteCreateResponseDTO> send(
			@RequestHeader("X-User") Long userId,
			@Valid @RequestBody NoteCreateRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(noteService.send(userId, request.content()));
	}

	@PatchMapping("/read")
	public ResponseEntity<Void> readAll(@RequestHeader("X-User") Long userId) {
		noteService.readAll(userId);
		return ResponseEntity.noContent().build();
	}
}
