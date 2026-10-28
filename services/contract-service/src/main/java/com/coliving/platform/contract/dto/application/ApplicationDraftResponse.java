package com.coliving.platform.contract.dto.application;

import com.coliving.platform.contract.entity.Contract;
import com.coliving.platform.contract.enums.ContractStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * [1-1 → 다음 단계] 응답 (201 Created)
 * 다음 화면에는 신청번호만 넘기고, 이후 화면은 이 번호로 DB에서 조회한다.
 */
public record ApplicationDraftResponse(
        String applicationNo,          // "CT-20261021-0042"
        ContractStatus status,         // DRAFT
        OffsetDateTime draftExpiresAt  // 작성 기한 (생성 + 2일)
) {
    public static ApplicationDraftResponse from(Contract c) {
        return new ApplicationDraftResponse(
                c.getApplicationNo(),
                c.getStatus(),
                c.getExpiresAt().atOffset(ZoneOffset.ofHours(9))
        );
    }
}
