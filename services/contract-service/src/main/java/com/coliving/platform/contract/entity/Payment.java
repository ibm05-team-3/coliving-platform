package com.coliving.platform.contract.entity;

import com.coliving.platform.contract.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 결제 시도마다 1행 (신청 1 : 결제 N).
 * PENDING → PAID는 정상 승인과 PENDING 보정이 겹칠 수 있으므로 엔티티 메서드가 아니라
 * PaymentRepository의 조건부 UPDATE(markPaidIfPending)로 처리한다.
 */
@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        //자동증가 번호

    @Column(name = "application_id", nullable = false)
    private Long applicationId;     //어느 신청의 결제인지

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;                             //우리쪽 주문번호, 카카오에 partner_order_id으로 전송

    @Column(name = "tid", unique = true, length = 100)
    private String tid;                                 //카카오가 발급하는 id ready 직후 저장

    @Column(name = "amount", nullable = false)
    private Long amount;        // 결제금액, Contract.firstPayment를 그대로 쓴다.

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;   //결제 상태

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;                       // MONEY / CARD (카카오 응답 값)

    @Column(name = "approved_at")
    private Instant approvedAt;     //결제가 승인된 시각

    @Column(name = "fail_reason", length = 500)
    private String failReason;      //결제 실패사유, 취소를 요청할때 취소 사유도 기록이된다.

    @Column(name = "canceled_at")
    private Instant canceledAt;     //환불이 완료된 시각

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** 1단계(트랜잭션 A): 결제하기 클릭 → 새 주문번호로 PENDING 저장. 금액은 신청의 고정 금액 */
    public static Payment start(Long applicationId, String orderId, long amount) {
        //새 주문번호로 결제 한줄을 만들고
        Payment p = new Payment();
        p.applicationId = applicationId;
        p.orderId = orderId;
        p.amount = amount;
        //상태를 pending으로 만듬.
        p.status = PaymentStatus.PENDING;
        return p;
    }

    /** ready 응답 직후 tid 저장 */
    public void saveTid(String tid) { //작동하려면  require조건에 맞아야함.
        requireStatus(PaymentStatus.PENDING, "PENDING 결제에만 tid를 저장할 수 있습니다");
        if (this.tid != null) throw new IllegalStateException("tid가 이미 저장되어 있습니다");
        this.tid = tid; //카카오 거래 id 저장
    }

    /** ready 실패, 승인 실패, 보정 결과 실패 */
    public void markFailed(String reason) {
        requireStatus(PaymentStatus.PENDING, "PENDING 결제만 실패 처리할 수 있습니다");
        this.status = PaymentStatus.FAILED;
        this.failReason = truncate(reason, 500); //사유를 500자 이내로 저장
    }

    /** 사용자가 결제창에서 중단 (cancel_url, 보정 결과 사용자 중단) */
    public void markCanceledByUser() { //취소키 눌렀을때
        requireStatus(PaymentStatus.PENDING, "PENDING 결제만 중단 처리할 수 있습니다");
        this.status = PaymentStatus.CANCELED;
    }

    /** 결제 후 취소 선점 (입주 전 취소 승인, completePayment에서 신청이 APPROVED가 아닐 때) */
    public void requestCancel(String reason) {//이미 결제된 건을 취소했을때
        requireStatus(PaymentStatus.PAID, "PAID 결제만 취소를 요청할 수 있습니다");
        this.status = PaymentStatus.CANCEL_REQUESTED;
        this.failReason = truncate(reason, 500);
    }

    /** 카카오 원결제 취소 성공 */
    public void markRefunded() {//결제 취소가 성공했을때
        requireStatus(PaymentStatus.CANCEL_REQUESTED, "취소 요청 중인 결제만 환불 완료 처리할 수 있습니다");
        this.status = PaymentStatus.REFUNDED;//상태를 변경
        this.canceledAt = Instant.now();// 시간정보를 기록
    }

    private void requireStatus(PaymentStatus expected, String message) { //contract 와 같이 문지기역할
        if (this.status != expected) throw new IllegalStateException(message);
    }

    private static String truncate(String s, int max) {//에러 메시지가 길면 잘라버린다.
        return (s == null || s.length() <= max) ? s : s.substring(0, max);
    }
}
