package com.coliving.platform.contract.dto.internal;

/**
 * 호실 서비스(gRPC)에서 받은 값을 엔티티로 넘길 때 쓰는 묶음.
 * 프론트가 보낸 값이 아니라 서버가 조회한 값만 담는다.
 */
public record RoomSnapshot(
        Long roomId,
        String buildingName,
        String roomName,
        long monthlyFee,
        long deposit,
        long discountAmount,     // ⚠ 10/27 할인 제외 → 사용처 정리 후 삭제
        String discountNote      // ⚠ 10/27 할인 제외 → 사용처 정리 후 삭제
) {}
