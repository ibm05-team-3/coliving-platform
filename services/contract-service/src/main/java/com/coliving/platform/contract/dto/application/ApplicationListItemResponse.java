package com.coliving.platform.contract.dto.application;

import com.coliving.platform.contract.enums.ContractStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * [나의 신청 목록] GET /contract-service/apply/applications
 *
 * 목록 한 줄. 상세 정보는 ApplicationDetailResponse로 따로 조회한다.
 * 응답은 List<ApplicationListItemResponse> (또는 팀 공통 페이지 형식)
 */
public record ApplicationListItemResponse(
        String applicationNo,
        ContractStatus status,
        String buildingName,
        String roomName,
        LocalDate moveInDate,
        Integer months,
        OffsetDateTime submittedAt
) {}
