package com.coliving.platform.safety.controller;

import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.dto.PageResponse;
import com.coliving.platform.safety.dto.AdminReportDetailDTO;
import com.coliving.platform.safety.dto.AdminReportListResponseDTO;
import com.coliving.platform.safety.dto.AdminSanctionItemDTO;
import com.coliving.platform.safety.dto.ReportActionRequestDTO;
import com.coliving.platform.safety.dto.ReportActionResponseDTO;
import com.coliving.platform.safety.dto.SanctionRevokeRequestDTO;
import com.coliving.platform.safety.dto.SanctionRevokeResponseDTO;
import com.coliving.platform.safety.entity.ReportSourceType;
import com.coliving.platform.safety.entity.ReportStatus;
import com.coliving.platform.safety.entity.ReportTargetType;
import com.coliving.platform.safety.entity.SanctionType;
import com.coliving.platform.safety.service.AdminReportService;
import com.coliving.platform.safety.service.AdminSanctionService;
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
 * 신고 · 제재 관리 — 관리자 (API 9-1 ~ 9-5)
 * 관리자 확인: 지금은 유저 정보의 role = ADMIN (TODO: Gateway 역할 헤더로 교체)
 */
@RestController
@RequestMapping("/community-service/admin")
@RequiredArgsConstructor
public class AdminReportController {

	private final AdminReportService adminReportService;
	private final AdminSanctionService adminSanctionService;
	private final ResidentGuard residentGuard;

	@GetMapping("/reports")
	public AdminReportListResponseDTO getReports(
			@RequestHeader("X-User") Long adminId,
			@RequestParam(required = false) ReportStatus status,
			@RequestParam(required = false) ReportSourceType sourceType,
			@RequestParam(required = false) ReportTargetType targetType,
			@RequestParam(required = false) Long buildingId,
			@RequestParam(defaultValue = "latest") String sort,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		residentGuard.requireAdmin(adminId);
		return adminReportService.getReports(status, sourceType, targetType, buildingId, sort, page, size);
	}

	@GetMapping("/reports/{reportId}")
	public AdminReportDetailDTO getReport(@RequestHeader("X-User") Long adminId, @PathVariable Long reportId) {
		residentGuard.requireAdmin(adminId);
		return adminReportService.getReport(reportId);
	}

	@PostMapping("/reports/{reportId}/actions")
	public ReportActionResponseDTO handle(
			@RequestHeader("X-User") Long adminId,
			@PathVariable Long reportId,
			@Valid @RequestBody ReportActionRequestDTO request) {
		residentGuard.requireAdmin(adminId);
		return adminReportService.handle(adminId, reportId, request);
	}

	@GetMapping("/sanctions")
	public PageResponse<AdminSanctionItemDTO> getSanctions(
			@RequestHeader("X-User") Long adminId,
			@RequestParam(defaultValue = "true") boolean activeOnly,
			@RequestParam(required = false) SanctionType sanctionType,
			@RequestParam(required = false) Long buildingId,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		residentGuard.requireAdmin(adminId);
		return adminSanctionService.getSanctions(activeOnly, sanctionType, buildingId, keyword, page, size);
	}

	@PatchMapping("/sanctions/{sanctionId}/revoke")
	public SanctionRevokeResponseDTO revoke(
			@RequestHeader("X-User") Long adminId,
			@PathVariable Long sanctionId,
			@Valid @RequestBody SanctionRevokeRequestDTO request) {
		residentGuard.requireAdmin(adminId);
		return adminSanctionService.revoke(adminId, sanctionId, request.revokeReason());
	}
}
