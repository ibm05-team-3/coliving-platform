package com.coliving.platform.contract.dto.sign;

import java.time.OffsetDateTime;

/**
 * [2 전자 서명] 응답 (201 Created)
 * 프론트는 이 응답을 받은 뒤 결제 화면으로 이동한다.
 */
public record SignatureResponse(
        Long signatureId,
        OffsetDateTime signedAt
) {}
