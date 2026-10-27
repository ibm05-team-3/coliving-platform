package com.coliving.platform.contract.dto.internal;

/**
 * 신청서 제출 시 신청자 스냅샷.
 * - name, phone: 멤버 서비스 값 그대로 (사용자가 수정 불가) → 필수
 * - email, address: 사용자가 수정 가능
 * DB 컬럼은 nullable이라 DB가 막아주지 않으므로 여기서 거른다.
 */
public record ApplicantInfo(
        String name,
        String phone,
        String email,
        String address
) {
    public ApplicantInfo {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("신청자 이름이 없습니다");
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("신청자 휴대폰 번호가 없습니다");
    }
}
