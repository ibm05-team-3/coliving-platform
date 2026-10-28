package com.coliving.dashboard.snapshot.controller;

import com.coliving.dashboard.snapshot.dto.DashboardSummaryResponse;
import com.coliving.dashboard.snapshot.service.DashboardSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.ZoneId;

/** 대시보드 화면의 지표 카드, 재무 요약, 추이 그래프에 데이터를 내려주는 API. */
@RestController
@RequestMapping("/dashboard/summary")
@RequiredArgsConstructor
public class DashboardSummaryController {

    private final DashboardSummaryService summaryService;

    /** 건물별 월 요약 조회. */
    @GetMapping
    public DashboardSummaryResponse summary(
            @RequestParam Long buildingId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {

        YearMonth target = month != null ? month : YearMonth.now(ZoneId.of("Asia/Seoul"));
        return summaryService.getSummary(buildingId, target);
    }
}