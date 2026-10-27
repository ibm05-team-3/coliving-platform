package com.coliving.platform.contract.dto.payment;

/**
 * [3 결제하기 버튼] POST /contract-service/apply/applications/{applicationNo}/payment/ready
 *
 * 금액은 보내지 않는다. 서버가 신청서의 스냅샷 금액으로 PG에 요청한다.
 */
public record PaymentReadyRequest(
        String method      // "KAKAOPAY" (나중에 "TOSS")
) {}
