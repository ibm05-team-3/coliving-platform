package com.coliving.dashboard.snapshot.service;

import com.coliving.dashboard.activitylog.document.ActivityLogDocument;
import com.coliving.dashboard.activitylog.repository.ActivityLogRepository;
import com.coliving.dashboard.roomstatus.document.RoomStatusDocument;
import com.coliving.dashboard.roomstatus.repository.RoomStatusRepository;
import com.coliving.dashboard.snapshot.document.DashboardSnapshotDocument;
import com.coliving.dashboard.snapshot.repository.DashboardSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 일별 집계 스냅샷을 만든다. 건물별로 하루 1건({건물ID}_{날짜})이며, 같은 날 다시 만들면 덮어쓴다.
 *
 * 호실 현황과 승인 대기 건수는 dashboard-service가 가진 데이터로 계산한다.
 * 연체, 미수금, 보증금, 환불 예정은 이벤트로 알 수 없으므로 직전 스냅샷 값을 이어받는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardSnapshotService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final DashboardSnapshotRepository snapshotRepository;
    private final RoomStatusRepository roomStatusRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ElasticsearchOperations operations;

    /** 계약 이벤트 등으로 호실 현황이 바뀐 뒤, 건물의 오늘 스냅샷을 다시 만든다. */
    public void refreshToday(Long buildingId, String buildingName) {
        saveToday(buildingId, buildingName, 0L);
    }

    /** 결제 완료 이벤트의 금액을 오늘 스냅샷의 당월 수납액에 더한다. 같은 결제를 두 번 넣지 않도록 호출하는 쪽에서 막는다. */
    public void addPayment(Long buildingId, String buildingName, long amount) {
        saveToday(buildingId, buildingName, amount);
    }

    /** 모든 건물의 오늘 스냅샷을 만든다. 날짜나 월이 바뀌어도 화면에 최신 스냅샷이 있게 매일 새벽에 실행한다. */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void refreshAllToday() {
        Map<Long, String> buildings = new LinkedHashMap<>();
        roomStatusRepository.findAll().forEach(room -> {
            if (room.getBuildingId() != null) {
                buildings.putIfAbsent(room.getBuildingId(), room.getBuildingName());
            }
        });
        buildings.forEach(this::refreshToday);
        log.info("일별 스냅샷 생성 완료: 건물 {}곳", buildings.size());
    }

    // 계약 이벤트와 결제 이벤트가 동시에 들어와도 수납액이 덮어써지지 않도록 한 번에 하나씩 처리한다.
    private synchronized void saveToday(Long buildingId, String buildingName, long paymentAmount) {
        // 방금 저장한 문서도 검색에 잡히도록 인덱스를 새로고침한다 (ES는 저장 직후 바로 검색되지 않을 수 있다).
        operations.indexOps(RoomStatusDocument.class).refresh();
        operations.indexOps(ActivityLogDocument.class).refresh();

        LocalDate today = LocalDate.now(KST);
        Instant now = Instant.now();

        // 오늘 스냅샷이 있으면 그것, 없으면 가장 최근 스냅샷
        DashboardSnapshotDocument latest = snapshotRepository
                .findFirstByBuildingIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(buildingId, today)
                .orElse(null);
        boolean sameDay = latest != null && latest.getSnapshotDate().equals(today);
        boolean sameMonth = latest != null
                && YearMonth.from(latest.getSnapshotDate()).equals(YearMonth.from(today));

        // 호실 현황으로 계산하는 값
        List<RoomStatusDocument> rooms = roomStatusRepository.findByBuildingId(buildingId, PageRequest.of(0, 1000));
        int total = rooms.size();
        int movingOut = (int) rooms.stream().filter(r -> "MOVING_OUT".equals(r.getStatus())).count();
        // 입주율은 퇴거 예정 호실도 입주로 본다 (es-setup.md의 더미 데이터와 같은 기준)
        int occupied = (int) rooms.stream()
                .filter(r -> "OCCUPIED".equals(r.getStatus()) || "MOVING_OUT".equals(r.getStatus()))
                .count();
        double occupancyRate = total == 0 ? 0.0 : Math.round(occupied * 1000.0 / total) / 10.0;

        // 활동 로그로 계산하는 값: 처리 대기 중인 계약 건
        int pendingApproval = (int) activityLogRepository
                .countByBuildingIdAndActivityTypeAndStatus(buildingId, "CONTRACT", "PENDING");

        // 당월 수납액은 같은 달이면 이어받아 결제 금액을 더한다.
        long collected = (sameMonth ? orZero(latest.getMonthCollectedAmount()) : 0L) + paymentAmount;

        snapshotRepository.save(DashboardSnapshotDocument.builder()
                .id(buildingId + "_" + today)
                .buildingId(buildingId)
                .buildingName(buildingName)
                .snapshotDate(today)
                .totalRooms(total)
                .occupiedRooms(occupied)
                .occupancyRate(occupancyRate)
                .residentCount(occupied)                 // 입주자는 1인 1실로 본다
                .movingOutCount(movingOut)
                .pendingApprovalCount(pendingApproval)
                // 이벤트로 알 수 없는 값은 직전 스냅샷을 이어받는다.
                .overdueCount(latest == null ? 0 : orZero(latest.getOverdueCount()))
                .monthCollectedAmount(collected)
                .monthUnpaidAmount(sameMonth ? orZero(latest.getMonthUnpaidAmount()) : 0L)
                .totalUnpaidAmount(latest == null ? 0L : orZero(latest.getTotalUnpaidAmount()))
                .depositHeldAmount(latest == null ? 0L : orZero(latest.getDepositHeldAmount()))
                .refundScheduledAmount(latest == null ? 0L : orZero(latest.getRefundScheduledAmount()))
                .refundScheduledCount(latest == null ? 0 : orZero(latest.getRefundScheduledCount()))
                .createdAt(sameDay && latest.getCreatedAt() != null ? latest.getCreatedAt() : now)
                .updatedAt(now)
                .build());
    }

    private static long orZero(Long value) {
        return value == null ? 0L : value;
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
