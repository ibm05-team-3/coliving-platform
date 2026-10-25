package com.coliving.platform.contract.enums;

/** payment.status CHECK 값. 신청 상태(ContractStatus)와 별개다. */
public enum PaymentStatus {
    PENDING,            // 결제 시도 생성 (ready 전후)
    PAID,               // 승인 완료
    FAILED,             // ready 실패, 승인 실패, 보정 결과 실패
    CANCELED,           // 사용자가 결제창에서 중단 (결제 안 됨)
    CANCEL_REQUESTED,   // 결제 후 취소를 선점한 상태, 카카오 취소 호출 전/중
    REFUNDED            // 카카오 원결제 취소 성공
}
