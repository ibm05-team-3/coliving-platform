package com.coliving.platform.contract.enums;

/** contract_application.status CHECK 값과 1:1. DB에는 이름 그대로(STRING) 저장된다. */
public enum ContractStatus {
    DRAFT,                      // 작성중 (기한 2일)
    SUBMITTED,                  // 심사중
    APPROVED,                   // 승인·결제대기 (기한 1일)
    PAID,                       // 결제완료
    COMPLETED,                  // 계약완료 (아웃박스 발행 후)
    REJECTED,                   // 반려 (최종, 재신청은 새 신청)
    CANCELED,                   // 심사 중 사용자 취소
    EXPIRED,                    // 기한 경과
    REFUNDED,                   // 입주 전 취소 → 환불 완료
    MOVE_IN_CANCEL_REQUESTED,   // 입주 전 취소 요청
    TERMINATION_REQUESTED,      // 퇴실 신청
    TERMINATED                  // 계약 종료
}
