package com.coliving.dashboard.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

/** 계약 서비스가 보내는 "계약 상태 변경" 이벤트, 호실 현황과 활동 로그를 함께 갱신하는 데 쓴다. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContractStatusChangedEvent(
        String eventId,             // 이벤트 고유 ID
        OffsetDateTime occurredAt,  // 상태가 바뀐 시간
        Long applicationId,         // 계약 ID
        String applicantName,       // 계약자 이름
        Long buildingId,            // 건물 ID
        String buildingName,        // 건물명
        Long roomId,                // 호실 ID
        String roomName,            // 호실명
        String previousStatus,      // 변경 전 계약 상태
        String currentStatus,       // 변경 후 계약 상태
        String moveInDate,          // 입주일 (yyyy-MM-dd)
        String moveOutDate          // 퇴실 예정일 (yyyy-MM-dd)
) {}