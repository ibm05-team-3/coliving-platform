package com.coliving.platform.contract.dto.application;

/**
 * [1-2 약관 전문보기] GET /contract-service/apply/terms/{termId}
 */
public record TermDetailResponse(
        Long termId,
        Integer version,
        String title,
        String content
) {}
