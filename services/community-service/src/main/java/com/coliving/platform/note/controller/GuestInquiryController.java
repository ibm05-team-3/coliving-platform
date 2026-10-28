package com.coliving.platform.note.controller;

import com.coliving.platform.note.dto.GuestInquiryRequestDTO;
import com.coliving.platform.note.dto.GuestInquiryResponseDTO;
import com.coliving.platform.note.service.GuestInquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 비회원 문의 (API 2-1) — 로그인 불필요 (Gateway에서 인증 제외 경로로 설정 필요) */
@RestController
@RequestMapping("/community-service/guest-inquiries")
@RequiredArgsConstructor
public class GuestInquiryController {

	private final GuestInquiryService guestInquiryService;

	@PostMapping
	public ResponseEntity<GuestInquiryResponseDTO> create(@Valid @RequestBody GuestInquiryRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(guestInquiryService.create(request));
	}
}
