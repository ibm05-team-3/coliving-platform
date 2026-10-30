package com.coliving.platform.safety.controller;

import com.coliving.platform.safety.dto.MySanctionDTO;
import com.coliving.platform.safety.dto.ReportCreateRequestDTO;
import com.coliving.platform.safety.dto.ReportCreateResponseDTO;
import com.coliving.platform.safety.service.ReportService;
import com.coliving.platform.safety.service.SanctionQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** 신고하기 (8-1) · 내 이용 제한 상태 (8-5) */
@RestController
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;
	private final SanctionQueryService sanctionQueryService;

	@PostMapping("/community-service/reports")
	public ResponseEntity<ReportCreateResponseDTO> create(
			@RequestHeader("X-User") Long userId,
			@Valid @RequestBody ReportCreateRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(userId, request));
	}

	@GetMapping("/community-service/sanctions/me")
	public MySanctionDTO getMySanction(@RequestHeader("X-User") Long userId) {
		return sanctionQueryService.getMySanction(userId);
	}
}
