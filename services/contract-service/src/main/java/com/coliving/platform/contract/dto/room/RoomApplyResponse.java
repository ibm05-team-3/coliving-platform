package com.coliving.platform.contract.dto.room;

import java.math.BigDecimal;
import java.util.List;

/**
 * [1-1 호실 선택] GET /contract-service/apply/rooms/{roomId}
 *
 * 호실 카드 + 오른쪽 신청 정보에 필요한 값.
 * 계약 기간 선택지(3/6/12개월)와 금액 계산은 할인이 없어서 응답에 넣지 않는다.
 *   첫 결제 금액 = monthlyFee + deposit (프론트 표시용, 결제 시 서버가 다시 계산)
 */
public record RoomApplyResponse(
        Long roomId,
        String buildingName,
        String region,               // "서울 성동구"
        String roomName,             // "302호"
        String roomType,             // "1인실"
        String thumbnailUrl,         // 사진 없으면 null → 프론트 기본 이미지
        List<String> roomFacilities, // ["전용 욕실", ...] 없으면 빈 배열
        BigDecimal area,             // 13.50 (㎡)
        Integer floorNumber,
        Long monthlyFee,
        Long deposit,
        Long managementFee,
        boolean available            // "입주 가능" 배지
) {}
