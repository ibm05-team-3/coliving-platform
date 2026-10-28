package com.coliving.dashboard.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

/** 공용 자원 서비스가 보내는 "예약 상태 변경" 이벤트, 활동 로그에 예약 건으로 기록하는 데 쓴다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservationStatusChangedEvent(
        String eventId,             // 이벤트 고유 ID
        OffsetDateTime occurredAt,  // 예약 상태가 바뀐 시간
        Long reservationId,         // 예약 ID
        Long buildingId,            // 건물 ID
        String buildingName,        // 건물명
        String resourceName,        // 공용 자원 이름 (예: 공용 주방)
        Long userId,                // 예약자 ID
        String reservationStatus    // PENDING(대기) / APPROVED(승인) / REJECTED(반려) / CANCELED(취소)
) {}