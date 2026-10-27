package com.coliving.platform.contract.dto.application;

import java.time.LocalDate;
import java.util.List;

/**
 * [1-2 신청자 정보] GET /contract-service/apply/applications/{applicationNo}/form
 *
 * DRAFT 신청서 + 회원 정보(멤버 서비스) + 약관 목록.
 * 이름·휴대폰은 화면에서 수정 불가, 이메일·주소만 수정 가능.
 * 본인 신청서가 아니면 403, DRAFT가 아니면 409.
 */
public record ApplyFormResponse(
        String applicationNo,

        // 오른쪽 신청 정보 (DRAFT에 저장된 값)
        String buildingName,
        String roomName,
        LocalDate moveInDate,
        Integer months,
        Long monthlyFee,
        Long deposit,
        Long firstPaymentAmount,   // monthlyFee + deposit

        // 신청자 정보 (멤버 서비스에서 불러옴)
        String name,               // 수정 불가
        String phone,              // 수정 불가
        String email,              // 수정 가능
        String address,            // 수정 가능, 없으면 null

        List<TermItem> terms
) {

    public record TermItem(
            Long termId,
            Integer version,   // 동의할 때 이 버전을 그대로 돌려보낸다
            String title,      // "[필수] 개인정보 수집 및 이용 동의"
            boolean required,
            boolean hasDetail  // "전문보기" 버튼 표시 여부
    ) {}
}
