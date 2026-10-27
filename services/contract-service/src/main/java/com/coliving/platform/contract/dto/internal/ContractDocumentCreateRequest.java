package com.coliving.platform.contract.dto.internal;

import java.time.LocalDate;

/**
 * ⚠ 사용 중단 (10/27)
 * 문서 서비스가 계약서를 만들지 않고 양식만 갖기로 바뀌어서 이 요청은 더 이상 보내지 않는다.
 * 계약서는 계약 서비스가 양식에 값을 채워 contract_documents에 직접 저장한다.
 * 사용처가 없으면 이 파일을 지운다.
 */
@Deprecated
public record ContractDocumentCreateRequest(
        Long applicationId,
        String language,        // "ko" / "en" → 문서 서비스가 양식 선택
        String applicantName,
        String applicantPhone,
        String applicantAddress,
        String buildingName,
        String roomName,
        String roomType,
        LocalDate moveInDate,
        LocalDate moveOutDate,
        Integer months,
        Long monthlyFee,
        Long deposit
) {}


