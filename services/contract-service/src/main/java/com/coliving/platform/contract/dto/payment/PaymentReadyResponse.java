package com.coliving.platform.contract.dto.payment;

/**
 * [3 결제하기 버튼] 응답
 *
 * 카카오페이: 서버가 결제 준비(ready)를 요청하고 받은 결제창 주소를 그대로 넘긴다.
 *   프론트는 PC면 redirectPcUrl, 모바일이면 redirectMobileUrl로 이동한다.
 *   카카오페이 tid는 서버에만 저장하고 프론트에 주지 않는다.
 */
public record PaymentReadyResponse(
        String orderId,            // 서버가 만든 주문번호 (카카오페이 partner_order_id)
        String method,             // "KAKAOPAY"
        String redirectPcUrl,      // 카카오페이 next_redirect_pc_url
        String redirectMobileUrl   // 카카오페이 next_redirect_mobile_url
) {}
