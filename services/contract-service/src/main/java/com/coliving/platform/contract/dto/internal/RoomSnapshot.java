package com.coliving.platform.contract.dto.internal;

import java.math.BigDecimal;

/**
 * 호실 서비스(gRPC)에서 받은 값을 엔티티로 넘길 때 쓰는 묶음.
 * 프론트가 보낸 값이 아니라 서버가 조회한 값만 담는다.
 *
 * Contract 엔티티의 호실 스냅샷 컬럼(NOT NULL)을 모두 채울 수 있도록 필드를 맞췄다.
 * roomStructure만 매물 쪽에서 없을 수 있어 null 허용.
 */
public record RoomSnapshot(
        Long roomId,
        Long buildingId,
        String buildingName,
        String region,
        String address,
        String roomName,
        String roomType,
        String roomStructure,      // 없으면 null
        int floorNumber,
        BigDecimal area,
        String roomFacilities,     // "전용 욕실, 발코니"
        String buildingFacilities, // "공용 라운지, 세탁실"
        long monthlyFee,
        long deposit,
        long managementFee
) {

    /** 매물 서비스 응답(DTO) → 스냅샷. ApplicationService에서만 쓴다 */
    public static RoomSnapshot from(RoomContractInfoDTO r) {
        return new RoomSnapshot(
                r.roomId(),
                r.buildingId(),
                r.buildingName(),
                r.region(),
                r.address(),
                r.roomName(),
                r.roomType(),
                r.roomStructure(),
                r.floorNumber() == null ? 0 : r.floorNumber(),
                r.area(),
                r.roomFacilitiesText(),
                r.buildingFacilitiesText(),
                r.monthlyFee(),
                r.deposit(),
                r.managementFee() == null ? 0L : r.managementFee()
        );
    }
}
