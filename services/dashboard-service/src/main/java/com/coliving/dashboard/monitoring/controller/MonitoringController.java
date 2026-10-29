package com.coliving.dashboard.monitoring.controller;

import com.coliving.dashboard.monitoring.dto.MonitoringChartsResponse;
import com.coliving.dashboard.monitoring.dto.MonitoringSummaryResponse;
import com.coliving.dashboard.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 모니터링 화면에 서비스 상태, 요청 지표 요약, 그래프 데이터를 내려주는 API. */
@RestController
@RequestMapping("/dashboard/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    /** 서비스 상태와 최근 5분 요청 지표 요약 조회. */
    @GetMapping("/summary")
    public MonitoringSummaryResponse summary() {
        return monitoringService.getSummary();
    }

    /** 최근 1시간 그래프(요청 수, 응답 시간 p95, 힙 메모리, CPU) 조회. */
    @GetMapping("/charts")
    public MonitoringChartsResponse charts() {
        return monitoringService.getCharts();
    }
}
