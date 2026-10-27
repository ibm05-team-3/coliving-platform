package com.coliving.platform.contract.dto.sign;

import java.time.OffsetDateTime;

/**
 * [2 본인 인증] 응답
 * 인증된 개인정보(이름, 생년월일 등)는 화면에 필요 없으므로 돌려주지 않는다.
 */
public record IdentityVerificationResponse(
        boolean verified,
        OffsetDateTime verifiedAt
) {}
