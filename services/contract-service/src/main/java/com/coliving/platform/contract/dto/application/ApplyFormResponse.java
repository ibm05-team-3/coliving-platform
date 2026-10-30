package com.coliving.platform.contract.dto.application;

import com.coliving.platform.contract.dto.internal.UserContractInfoDTO;
import com.coliving.platform.contract.entity.Contract;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * [1-2 신청자 정보] GET /contract-service/apply/applications/{applicationNo}/form
 *
 * DRAFT 신청서(DB에 저장된 호실·기간·금액) + 회원 정보(멤버 서비스 gRPC).
 * 이름·휴대폰은 화면에서 수정 불가, 이메일·주소만 수정 가능.
 * 본인 신청서가 아니면 403, DRAFT가 아니면 409.
 *
 * 호실 정보도 같이 내려주는 이유: 이 화면에서 새로고침해도 1-1 화면을 거치지 않고 그대로 보여주기 위해.
 */
public record ApplyFormResponse(
        String applicationNo,
        OffsetDateTime draftExpiresAt,   // 작성 기한 (생성 + 2일)

        // 오른쪽 신청 정보 (DRAFT에 저장된 값)
        Long roomId,                     // "이전" 버튼 → 1-1 화면으로 돌아갈 때 사용
        String buildingName,
        String region,
        String roomName,
        String roomType,
        LocalDate moveInDate,
        LocalDate moveOutDate,
        Integer months,
        Long monthlyFee,
        Long managementFee,
        Long deposit,
        Long firstPaymentAmount,         // 서버 계산값 (Contract.firstPayment)

        // 신청자 정보 (멤버 서비스에서 불러옴)
        String name,                     // 수정 불가
        String phone,                    // 수정 불가
        String email,                    // 수정 가능
        String address,                  // 수정 가능, 없으면 null

        String contractLanguage,         // 아직 고르지 않았으면 null → 프론트 기본값 "ko"
        String termsVersion              // 지금 동의받을 약관 버전. 제출할 때 그대로 돌려보낸다
) {

    public static ApplyFormResponse of(Contract c, UserContractInfoDTO u) {
        return new ApplyFormResponse(
                c.getApplicationNo(),
                c.getExpiresAt() == null ? null : c.getExpiresAt().atOffset(ZoneOffset.ofHours(9)),
                c.getRoomId(),
                c.getBuildingName(),
                c.getRegion(),
                c.getRoomName(),
                c.getRoomType(),
                c.getMoveInDate(),
                c.getMoveOutDate(),
                c.getContractMonths() == null ? null : c.getContractMonths().intValue(),
                c.getMonthlyFee(),
                c.getManagementFee(),
                c.getDeposit(),
                c.getFirstPayment(),
                u.name(),
                u.phone(),
                blankToNull(u.email()),
                blankToNull(u.address()),     // proto3는 값이 없으면 "" → null로 바꿔서 내려줌
                c.getContractLanguage(),
                Contract.CURRENT_TERMS_VERSION
        );
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public record TermItem(
            Long termId,
            Integer version,   // 동의할 때 이 버전을 그대로 돌려보낸다
            String title,      // "[필수] 개인정보 수집 및 이용 동의"
            boolean required,
            boolean hasDetail  // "전문보기" 버튼 표시 여부
    ) {}
}
