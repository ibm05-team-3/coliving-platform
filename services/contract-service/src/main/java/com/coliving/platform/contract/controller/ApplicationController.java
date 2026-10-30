package com.coliving.platform.contract.controller;

import com.coliving.platform.contract.dto.application.ApplicationDraftRequest;
import com.coliving.platform.contract.dto.application.ApplicationDraftResponse;
import com.coliving.platform.contract.dto.application.ApplicationSubmitRequest;
import com.coliving.platform.contract.dto.application.ApplicationSubmitResponse;
import com.coliving.platform.contract.dto.application.ApplyFormResponse;
import com.coliving.platform.contract.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 신청 1단계 API. gateway 경로: /contract-service/apply/applications/**
 *
 * 회원 번호는 X-User 헤더로만 받는다.
 * - gateway가 로그인 토큰을 검증한 뒤 토큰의 회원 ID를 X-User에 넣어준다
 * - 로그인하지 않은 요청이 X-User를 직접 넣어 보내면 gateway가 지운다 → 위조 불가
 * - 헤더가 없으면 CustomControllerAdvice가 401로 응답
 * ⚠ contract-service 포트(8089)로 직접 부르면 X-User를 마음대로 넣을 수 있다. 개발 중에만 직접 호출할 것
 */
@RestController
@RequestMapping("/apply/applications")
public class ApplicationController {

    private static final String USER_HEADER = "X-User";   // api-gateway UserIdHeaderWebFilter.SUBJECT_HEADER와 같아야 함

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // 1-1 "다음 단계": DRAFT 생성 (같은 호실 DRAFT가 있으면 그걸 수정해서 돌려줌)
    @PostMapping
    public ResponseEntity<ApplicationDraftResponse> createDraft(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestBody ApplicationDraftRequest req) {

        ApplicationDraftResponse res = applicationService.createDraft(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);   // 201
    }

    // 1-2 신청자 정보 화면
    @GetMapping("/{applicationNo}/form")
    public ApplyFormResponse getForm(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable String applicationNo) {

        return applicationService.getForm(userId, applicationNo);
    }

    // 1-2 "신청서 제출": DRAFT → SUBMITTED
    @PostMapping("/{applicationNo}/submit")
    public ApplicationSubmitResponse submit(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable String applicationNo,
            @RequestBody ApplicationSubmitRequest req) {

        return applicationService.submit(userId, applicationNo, req);
    }
}
