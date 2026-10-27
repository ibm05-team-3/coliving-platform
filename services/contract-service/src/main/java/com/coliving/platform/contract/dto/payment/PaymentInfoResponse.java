package com.coliving.platform.contract.dto.payment;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * [3 결제 화면 진입] GET /contract-service/apply/applications/{applicationNo}/payment
 *
 * 결제 버튼을 누르기 전에 화면에 보여줄 값.
 * 서명 완료 + APPROVED + 기한 안일 때만 조회 가능 (그 외 409).
 * 주문번호(orderId)는 여기서 주지 않고, 결제 준비(ready) 때 만든다.
 */
public record PaymentInfoResponse(
        // 결제 금액
        Long deposit,
        Long firstMonthFee,
        Long totalAmount,             // deposit + firstMonthFee

        // 오른쪽 신청 정보
        String buildingName,
        String roomName,
        LocalDate moveInDate,
        LocalDate moveOutDate,

        // 지금 쓸 수 있는 결제 수단. 처음엔 ["KAKAOPAY"]
        // 토스를 붙이면 서버 설정만 바꿔서 ["KAKAOPAY", "TOSS"] → 프론트는 이 목록대로 버튼을 그린다
        List<String> availableMethods,

        // 남은 시간 카운트다운: 사용자 PC 시계 대신 서버 시각 기준으로 계산
        //   남은 시간 = paymentDeadline - serverTime (응답 받은 순간부터 프론트가 1초씩 줄임)
        OffsetDateTime paymentDeadline,
        OffsetDateTime serverTime
) {}
