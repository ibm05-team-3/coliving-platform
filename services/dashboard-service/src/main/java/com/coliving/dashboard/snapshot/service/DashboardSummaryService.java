package com.coliving.dashboard.snapshot.service;

import com.coliving.dashboard.snapshot.document.DashboardSnapshotDocument;
import com.coliving.dashboard.snapshot.dto.DashboardSummaryResponse;
import com.coliving.dashboard.snapshot.dto.DashboardSummaryResponse.Cards;
import com.coliving.dashboard.snapshot.dto.DashboardSummaryResponse.Finance;
import com.coliving.dashboard.snapshot.dto.DashboardSummaryResponse.TrendPoint;
import com.coliving.dashboard.snapshot.repository.DashboardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** 일별 스냅샷을 조합해 대시보드 화면용 요약(지표 카드, 재무 요약, 추이)을 만든다. */
@Service
@RequiredArgsConstructor
public class DashboardSummaryService {

    private final DashboardSnapshotRepository repository;

    /** 건물의 조회 월 요약을 만든다. */
    public DashboardSummaryResponse getSummary(Long buildingId, YearMonth month) {
        // 조회 월의 가장 최근 스냅샷
        DashboardSnapshotDocument current = findInMonth(buildingId, month)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "해당 월의 집계 데이터가 없습니다."));

        // 증감 계산용 비교 대상: 지난달 스냅샷, 일주일 전 스냅샷
        DashboardSnapshotDocument prevMonth = findInMonth(buildingId, month.minusMonths(1)).orElse(null);
        DashboardSnapshotDocument weekAgo = repository
                .findFirstByBuildingIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                        buildingId, current.getSnapshotDate().minusDays(7))
                .orElse(null);

        Cards cards = new Cards(
                current.getOccupancyRate(),
                prevMonth == null ? null : diff(current.getOccupancyRate(), prevMonth.getOccupancyRate()),
                current.getMonthCollectedAmount(),
                prevMonth == null ? null : changeRate(current.getMonthCollectedAmount(), prevMonth.getMonthCollectedAmount()),
                current.getPendingApprovalCount(),
                current.getOverdueCount(),
                weekAgo == null ? null : current.getOverdueCount() - weekAgo.getOverdueCount()
        );

        Finance finance = new Finance(
                current.getTotalUnpaidAmount(),
                current.getOverdueCount(),
                current.getDepositHeldAmount(),
                current.getResidentCount(),
                current.getRefundScheduledAmount(),
                current.getRefundScheduledCount()
        );

        // 조회 월을 포함한 최근 6개월.
        List<TrendPoint> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = month.minusMonths(i);
            DashboardSnapshotDocument s = findInMonth(buildingId, m).orElse(null);
            trend.add(new TrendPoint(
                    m.toString(),
                    s == null ? null : s.getOccupancyRate(),
                    s == null ? null : s.getMonthCollectedAmount(),
                    s == null ? null : s.getMonthUnpaidAmount()));
        }

        return new DashboardSummaryResponse(
                buildingId, current.getBuildingName(), month.toString(), cards, finance, trend);
    }

    /** 해당 월 안에서 가장 최근 스냅샷을 찾는다. */
    private Optional<DashboardSnapshotDocument> findInMonth(Long buildingId, YearMonth month) {
        return repository
                .findFirstByBuildingIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
                        buildingId, month.atEndOfMonth())
                .filter(s -> YearMonth.from(s.getSnapshotDate()).equals(month));
    }

    /** 두 값의 차이를 소수 첫째 자리로 반환한다. */
    private Double diff(Double current, Double previous) {
        if (current == null || previous == null) return null;
        return round1(current - previous);
    }

    /** 이전 값 대비 증감률을 소수 첫째 자리로 반환한다. */
    private Double changeRate(Long current, Long previous) {
        if (current == null || previous == null || previous == 0) return null;
        return round1((current - previous) * 100.0 / previous);
    }

    /** 소수 첫째 자리 반올림. */
    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}