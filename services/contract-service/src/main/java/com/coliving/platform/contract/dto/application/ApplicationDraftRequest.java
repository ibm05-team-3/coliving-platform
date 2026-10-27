package com.coliving.platform.contract.dto.application;

import java.time.LocalDate;

/**
 * [1-1 호실 선택 → 다음 단계] POST /contract-service/apply/applications
 *
 * DRAFT(작성중, 기한 2일) 신청서를 만든다.
 * 보내지 않는 값과 이유
 *   - memberId : JWT에서 꺼낸다
 *   - monthlyFee, deposit : 서버가 매물 서비스(gRPC) 값으로 저장한다 (조작 방지)
 *   - moveOutDate : moveInDate + months - 1일로 서버가 계산한다
 * "상담 신청" 버튼은 DRAFT를 만들지 않으므로 이 API를 부르지 않는다.
 */
public record ApplicationDraftRequest(
        Long roomId,
        LocalDate moveInDate,     // "2026-11-02"
        Integer months            // 3 / 6 / 12
) {}
