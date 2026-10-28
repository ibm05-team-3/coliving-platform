package com.coliving.platform.contract.controller;

import com.coliving.platform.contract.dto.application.ApplicationDraftRequest;
import com.coliving.platform.contract.dto.application.ApplicationDraftResponse;
import com.coliving.platform.contract.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apply/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationDraftResponse> createDraft(
            @RequestHeader("X-Member-Id") Long memberId,     // JWT 붙기 전 임시
            @RequestBody ApplicationDraftRequest req) {

        ApplicationDraftResponse res = applicationService.createDraft(memberId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);   // 201
    }
}
