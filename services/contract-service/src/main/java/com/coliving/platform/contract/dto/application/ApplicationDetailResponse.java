package com.coliving.platform.contract.dto.application;

import com.coliving.platform.contract.enums.ContractStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * [접수 완료 / 나의 신청 상세 / 계약 완료] GET /contract-service/apply/applications/{applicationNo}
 *
 * 화면 3곳이 같은 신청서 1건을 보여주므로 응답 하나로 쓴다.
 * 상태에 따라 값이 없는 필드는 null.
 * 본인 신청서인지는 JWT의 회원과 신청서의 회원을 비교해서 확인한다 (아니면 403).
 */
public record ApplicationDetailResponse(
        String applicationNo,
        ContractStatus status,      // JSON에는 "SUBMITTED"처럼 enum 이름 문자열로 나간다

        // 호실 (신청 시점 스냅샷)
        String buildingName,
        String roomName,
        String roomType,

        // 기간
        LocalDate moveInDate,
        LocalDate moveOutDate,      // 입주일 + 개월 - 1일
        Integer months,

        // 금액 (신청 시점 스냅샷)
        Long monthlyFee,
        Long deposit,
        Long firstPaymentAmount,    // monthlyFee + deposit

        OffsetDateTime submittedAt,
        String rejectReason,        // REJECTED일 때만
        OffsetDateTime paymentDeadline, // APPROVED일 때만 (승인 + 1일)

        PaymentSummary payment      // PAID 이후에만, 그 전엔 null
) {

    /** [계약 완료] 화면의 결제 정보 */
    public record PaymentSummary(
            String method,          // "카카오페이"
            Long paidAmount,
            OffsetDateTime paidAt,
            String receiptUrl       // 영수증 보기 버튼. PG가 주는 주소
    ) {}
}
