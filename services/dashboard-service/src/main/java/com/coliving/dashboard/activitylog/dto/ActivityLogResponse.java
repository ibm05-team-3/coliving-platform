package com.coliving.dashboard.activitylog.dto;

import com.coliving.dashboard.activitylog.document.ActivityLogDocument;

import java.time.Instant;

/** 화면의 "최근 활동 로그" 한 줄에 내려주는 응답. */
public record ActivityLogResponse(
        String id,              // 활동 로그 ID ({유형}-{원본 ID}, 예: CONTRACT-501)
        String activityType,    // CONTRACT(계약) / RESERVATION(예약) / MOVE_IN(입주) / PAYMENT(결제)
        String content,         // 화면에 표시할 내용
        String status,          // COMPLETED(완료) / PENDING(대기) / PROCESSING(처리중)
        Instant updatedAt       // 마지막 상태 변경 시간
) {
    /** ES 저장 데이터를 화면 응답으로 변환한다. */
    public static ActivityLogResponse from(ActivityLogDocument doc) {
        return new ActivityLogResponse(doc.getId(), doc.getActivityType(), doc.getContent(), doc.getStatus(), doc.getUpdatedAt());
    }
}