package com.coliving.platform.contract.dto.internal;

import java.math.BigDecimal;
import java.util.List;

/**
 * 매물 서비스(gRPC)에서 받은 호실 정보를 계약 서비스 안에서 쓰는 형태로 옮긴 DTO.
 *
 * gRPC가 만들어주는 클래스(RoomContractInfo)를 서비스·엔티티 곳곳에서 직접 쓰지 않고
 * RoomQueryService에서 이 DTO로 바꿔서 넘긴다.
 * → 나중에 통신 방식이 또 바뀌어도 RoomQueryService만 고치면 된다.
 */
public record RoomContractInfoDTO(

        // ── 식별 ──────────────────────────────
        Long roomId,
        Long buildingId,

        // ── 화면 표시 ─────────────────────────
        List<String> imageUrls,          // 호실 사진 (첫 번째 = 대표 사진)
        String buildingName,
        String address,
        String region,
        String roomName,
        String roomType,
        List<String> roomFacilities,     // 편의시설 이름 목록
        List<String> buildingFacilities, // 건물 공용시설 이름 목록
        BigDecimal area,                 // proto에서는 "13.50" 문자열로 받아 BigDecimal로 변환
        Integer floorNumber,
        Long monthlyFee,
        Long deposit,

        // ── 계약·결제 로직용 ───────────────────
        Long managementFee,
        String rentalType,               // MONTHLY / SHORT_TERM / OTHER
        Integer capacity,
        String roomStructure,            // 없으면 null
        Boolean deleted
) {

    /** 대표 사진 (없으면 null → 프론트에서 기본 이미지 처리) */
    public String thumbnailUrl() {
        return (imageUrls == null || imageUrls.isEmpty()) ? null : imageUrls.get(0);
    }

    /** 계약 테이블 room_facilities 컬럼(TEXT)에 넣을 문자열 */
    public String roomFacilitiesText() {
        return String.join(", ", roomFacilities);
    }

    /** 계약 테이블 building_facilities 컬럼(TEXT)에 넣을 문자열 */
    public String buildingFacilitiesText() {
        return String.join(", ", buildingFacilities);
    }
}
