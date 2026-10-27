package com.coliving.platform.contract.dto.sign;

import java.time.LocalDate;

/**
 * [2 계약서 확인·서명] GET /contract-service/apply/applications/{applicationNo}/document
 *
 * APPROVED 상태에서만 조회 가능 (그 외 409).
 * 계약서는 계약 서비스의 contract_documents에 있는 완성본 텍스트.
 */
public record ContractDocumentResponse(
        String applicationNo,

        // 계약서 뷰어
        Long documentId,
        String language,            // "ko" / "en"
        String content,             // 양식에 데이터를 채운 완성본
        String contentHash,         // 서명할 때 그대로 돌려보낸다

        // 오른쪽 신청 정보
        String buildingName,
        String roomName,
        LocalDate moveInDate,
        LocalDate moveOutDate,
        Long firstPaymentAmount,

        // 버튼 활성화 조건 (새로고침해도 진행 상태 유지)
        boolean identityVerified,
        boolean signed
) {}
