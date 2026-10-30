package com.coliving.dashboard.monitoring.dto;

import java.util.List;

/** 관리자 모니터링 화면의 요약 카드와 서비스별 상태를 한 번에 내려주는 응답. */
public record MonitoringSummaryResponse(
        boolean prometheusAvailable,   // Prometheus 조회 성공 여부 (false면 cards는 null, services는 빈 목록)
        Cards cards,                   // 상단 요약 카드
        List<ServiceStatus> services,  // 서비스별 상태
        String checkedAt               // 조회 시각 (ISO-8601, 서울 기준)
) {

    /** 요약 카드 4개. 값이 아직 없으면(요청 없음 등) 해당 항목은 null. */
    public record Cards(
            Integer upCount,            // 정상 서비스 수
            Integer totalCount,         // 수집 대상 서비스 수
            Double avgResponseMs,       // 최근 5분 평균 응답 시간(ms)
            Double requestsPerMinute,   // 최근 5분 기준 분당 요청 수
            Double errorRatePercent     // 최근 5분 5xx 오류율(%)
    ) {}

    /** 서비스 하나의 상태. */
    public record ServiceStatus(
            String name,       // 서비스 이름 (Prometheus의 service 라벨)
            String instance,   // 수집 대상 주소 (host:port)
            boolean up         // 정상 여부
    ) {}

    /** Prometheus에 연결하지 못했을 때의 응답. */
    public static MonitoringSummaryResponse unavailable(String checkedAt) {
        return new MonitoringSummaryResponse(false, null, List.of(), checkedAt);
    }
}
