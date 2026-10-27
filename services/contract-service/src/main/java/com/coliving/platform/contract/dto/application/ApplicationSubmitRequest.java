package com.coliving.platform.contract.dto.application;

import java.util.List;

/**
 * [1-2 신청서 제출] POST /contract-service/apply/applications/{applicationNo}/submit
 *
 * DRAFT → SUBMITTED. 이 시점에 결제 금액이 고정된다.
 * 이름·휴대폰은 수정 불가라 받지 않는다 (서버가 멤버 서비스 값을 스냅샷으로 저장).
 */
public record ApplicationSubmitRequest(
        String email,
        String address,
        String contractLang,          // "ko" / "en"
        List<AgreedTerm> agreedTerms
) {

    /** 동의한 약관. 화면에 보였던 버전을 그대로 보내야 "어떤 버전에 동의했는지" 증명된다 */
    public record AgreedTerm(
            Long termId,
            Integer version
    ) {}
}
