package com.coliving.dashboard.snapshot.dto;

import java.util.List;

/** 대시보드 화면의 지표 카드, 재무 요약, 6개월 추이를 한 번에 내려주는 응답. */
public record DashboardSummaryResponse(
        Long buildingId,           // 건물 ID
        String buildingName,       // 건물명
        String month,              // 조회 월 (yyyy-MM)
        Cards cards,               // 핵심 지표 카드
        Finance finance,           // 재무 요약 카드
        List<TrendPoint> trend     // 최근 6개월 추이
) {

    /** 핵심 지표 카드 4개 (입주율, 수납액, 승인 대기, 연체). */
    public record Cards(
            Double occupancyRate,          // 입주율
            Double occupancyRateDiff,      // 입주율 지난달 대비 증감
            Long monthCollectedAmount,     // 이번 달 수납액
            Double collectedChangeRate,    // 수납액 지난달 대비 증감률
            Integer pendingApprovalCount,  // 승인 대기 신청 수
            Integer overdueCount,          // 연체 계약 수
            Integer overdueCountDiff       // 연체 계약 지난주 대비 증감
    ) {}

    /** 재무 요약 카드 (미수금, 보증금 보관액, 환불 예정). */
    public record Finance(
            Long totalUnpaidAmount,        // 미수금 총액
            Integer overdueCount,          // 연체 건수
            Long depositHeldAmount,        // 보증금 보관액
            Integer residentCount,         // 입주자 수
            Long refundScheduledAmount,    // 환불 예정 금액
            Integer refundScheduledCount   // 환불 예정 건수
    ) {}

    /** 6개월 추이 그래프의 점 하나. */
    public record TrendPoint(
            String month,                  // 월 (yyyy-MM)
            Double occupancyRate,          // 입주율
            Long collectedAmount,          // 수납액
            Long unpaidAmount              // 미수액
    ) {}
}