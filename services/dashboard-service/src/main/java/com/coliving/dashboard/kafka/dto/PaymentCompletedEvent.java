package com.coliving.dashboard.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

/** 계약 서비스가 보내는 "결제 완료" 이벤트, 활동 로그에 결제 건으로 기록하는 데 쓴다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentCompletedEvent(
        String eventId,             // 이벤트 고유 ID
        OffsetDateTime occurredAt,  // 결제 승인 시간
        Long paymentId,             // 결제 ID
        Long applicationId,         // 계약 ID
        String applicantName,       // 계약자 이름
        Long buildingId,            // 건물 ID
        String buildingName,        // 건물명
        String roomName,            // 호실명
        Long amount                 // 결제 금액
) {}