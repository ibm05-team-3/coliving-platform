package com.coliving.platform.contract.dto.sign;

/**
 * [2 본인 인증] POST /contract-service/apply/applications/{applicationNo}/identity-verification
 *
 * 프론트가 포트원 인증창을 띄우고, 끝나면 받은 인증 ID만 서버로 보낸다.
 * 이름·휴대폰 같은 인증 결과는 프론트가 보내지 않는다.
 * 서버가 이 ID로 포트원에 직접 조회해서 확인한다 (프론트 값은 위조될 수 있음).
 */
public record IdentityVerificationRequest(
        String identityVerificationId
) {}
