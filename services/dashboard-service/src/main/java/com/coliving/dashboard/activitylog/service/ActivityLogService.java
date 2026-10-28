package com.coliving.dashboard.activitylog.service;

import com.coliving.dashboard.activitylog.document.ActivityLogDocument;
import com.coliving.dashboard.activitylog.dto.ActivityLogResponse;
import com.coliving.dashboard.activitylog.repository.ActivityLogRepository;
import com.coliving.dashboard.kafka.dto.ContractStatusChangedEvent;
import com.coliving.dashboard.kafka.dto.PaymentCompletedEvent;
import com.coliving.dashboard.kafka.dto.ReservationStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** 계약·결제·예약 이벤트를 활동 로그로 저장하고, 화면에 보여줄 최근 목록을 조회한다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    /** 계약 상태 변경 이벤트를 활동 로그로 저장한다. */
    public void saveContractActivity(ContractStatusChangedEvent event) {
        String place = event.buildingName() + " " + event.roomName();
        String type = "CONTRACT";
        String status;
        String content;

        switch (event.currentStatus()) {
            case "SUBMITTED" -> {                       // 계약 신청 접수
                status = "PENDING";
                content = place + " 계약 신청 (" + event.applicantName() + ")";
            }
            case "APPROVED" -> {                        // 관리자 승인
                status = "COMPLETED";
                content = place + " 계약 승인";
            }
            case "REJECTED" -> {                        // 관리자 반려
                status = "COMPLETED";
                content = place + " 계약 반려";
            }
            case "COMPLETED" -> {                       // 입주 확정
                type = "MOVE_IN";
                status = "COMPLETED";
                content = place + " 입주 완료";
            }
            case "TERMINATION_REQUESTED" -> {           // 퇴실 요청
                status = "PENDING";
                content = place + " 퇴실 요청 (" + event.applicantName() + ")";
            }
            case "TERMINATED" -> {                      // 계약 해지 확정
                status = "COMPLETED";
                content = place + " 계약 해지";
            }
            default -> {
                log.debug("활동 로그 대상이 아닌 계약 상태: {}", event.currentStatus());
                return;
            }
        }

        save(type + "-" + event.applicationId(), type, content, status,
                event.eventId(), event.buildingId(), event.buildingName(),
                "contract-service", String.valueOf(event.applicationId()),
                event.occurredAt().toInstant());
    }

    /** 결제 완료 이벤트를 활동 로그로 저장한다. 결제 ID 기준으로 1건씩 쌓인다. */
    public void savePaymentActivity(PaymentCompletedEvent event) {
        String content = event.buildingName() + " " + event.roomName()
                + " 결제 완료 (" + event.applicantName() + ", "
                + String.format("%,d", event.amount()) + "원)";

        save("PAYMENT-" + event.paymentId(), "PAYMENT", content, "COMPLETED",
                event.eventId(), event.buildingId(), event.buildingName(),
                "contract-service", String.valueOf(event.paymentId()),
                event.occurredAt().toInstant());
    }

    /** 예약 상태 변경 이벤트를 활동 로그로 저장한다. */
    public void saveReservationActivity(ReservationStatusChangedEvent event) {
        String status;
        String content;
        String who = "회원 " + event.userId();

        switch (event.reservationStatus()) {
            case "PENDING" -> {
                status = "PENDING";
                content = event.resourceName() + " 예약 신청 (" + who + ")";
            }
            case "APPROVED" -> {
                status = "COMPLETED";
                content = event.resourceName() + " 예약 승인 (" + who + ")";
            }
            case "REJECTED" -> {
                status = "COMPLETED";
                content = event.resourceName() + " 예약 반려 (" + who + ")";
            }
            case "CANCELED" -> {
                status = "COMPLETED";
                content = event.resourceName() + " 예약 취소 (" + who + ")";
            }
            default -> {
                log.debug("활동 로그 대상이 아닌 예약 상태: {}", event.reservationStatus());
                return;
            }
        }

        save("RESERVATION-" + event.reservationId(), "RESERVATION", content, status,
                event.eventId(), event.buildingId(), event.buildingName(),
                "amenity-service", String.valueOf(event.reservationId()),
                event.occurredAt().toInstant());
    }

    /** 건물별 최근 활동 로그를 최근 변경 순으로 조회한다. */
    public List<ActivityLogResponse> getRecent(Long buildingId, int size) {
        int limit = Math.min(Math.max(size, 1), 50);
        return repository.findByBuildingIdOrderByUpdatedAtDesc(buildingId, PageRequest.of(0, limit))
                .stream()
                .map(ActivityLogResponse::from)
                .toList();
    }

    /** 활동 로그를 ES에 저장한다. */
    private void save(String id, String type, String content, String status,
                      String eventId, Long buildingId, String buildingName,
                      String sourceService, String sourceRefId, Instant occurredAt) {

        // 같은 건이 이미 있으면 최초 발생 시간은 유지
        Instant firstOccurredAt = repository.findById(id)
                .map(ActivityLogDocument::getOccurredAt)
                .orElse(occurredAt);

        try {
            repository.save(ActivityLogDocument.builder()
                    .id(id)
                    .version(occurredAt.toEpochMilli())
                    .eventId(eventId)
                    .buildingId(buildingId)
                    .buildingName(buildingName)
                    .activityType(type)
                    .content(content)
                    .status(status)
                    .sourceService(sourceService)
                    .sourceRefId(sourceRefId)
                    .occurredAt(firstOccurredAt)
                    .updatedAt(occurredAt)
                    .build());
        } catch (OptimisticLockingFailureException e) {
            log.info("이미 같거나 더 최신인 활동 로그가 있어 무시합니다: id={}", id);
        }
    }
}