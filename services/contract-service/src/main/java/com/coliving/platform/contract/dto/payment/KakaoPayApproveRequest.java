package com.coliving.platform.contract.dto.payment;

/**
 * [3 카카오페이 결제 승인] POST /contract-service/apply/applications/{applicationNo}/payment/kakaopay/approve
 *
 * 사용자가 카카오페이 결제창에서 결제하면 approval_url(프론트 화면)로 돌아오면서
 * 주소 뒤에 pg_token이 붙어 온다. 프론트는 그 값을 읽어 이 API로 보낸다.
 *   approval_url 예: {FRONT_URL}/contract/pay/approve?applicationNo=CT-20261021-0042&orderId=...  (+ &pg_token=...)
 *
 * 서버는 orderId로 저장해 둔 tid를 찾아 카카오페이 승인(approve)을 요청한다.
 *
 * 토스를 붙일 때는 이 DTO를 고치지 않고 TossConfirmRequest + /payment/toss/confirm을 새로 만든다.
 * 응답은 PG와 상관없이 PaymentResultResponse로 같다.
 */
public record KakaoPayApproveRequest(
        String orderId,
        String pgToken
) {}
