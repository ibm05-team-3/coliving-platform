package com.coliving.platform.contract.dto.payment;

import com.coliving.platform.contract.enums.ContractStatus;

import java.time.OffsetDateTime;

/**
 * [3 결제 승인] 응답 — 카카오페이·토스 공통
 *
 * 프론트는 이 응답을 받으면 계약 완료 화면으로 이동하고,
 * 완료 화면은 ApplicationDetailResponse로 다시 조회한다.
 */
public record PaymentResultResponse(
        ContractStatus status,  // PAID
        String method,          // "KAKAOPAY"
        Long paidAmount,
        OffsetDateTime paidAt
) {}
