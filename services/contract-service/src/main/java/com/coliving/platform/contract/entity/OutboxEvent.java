package com.coliving.platform.contract.entity;

import com.coliving.platform.contract.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * 아웃박스. 결제 확정(completePayment)과 같은 트랜잭션에서 저장하고, 발행 작업이 Kafka로 보낸다.
 * 같은 신청의 PaymentCompleted는 1건만 저장된다 (uq_outbox_payment_completed).
 */
@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    public static final String PAYMENT_COMPLETED = "PaymentCompleted";
    private static final int LAST_ERROR_MAX = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        //자동증가

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;                               // 소비자 중복 처리용(고유id)

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;                           // 신청 ID (Kafka 메시지 키)

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;       //메시지 종류, PaymentCompleted 하나뿐이다.

    // JSONB: String을 그대로 JSON으로 저장. JdbcTypeCode가 없으면 varchar로 보내서 타입 에러가 난다
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private String payload;     // 메시지 내용(JSON)- 입주담당과 맞춰야함

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;        //PENDING->PUBLISHED OR FAILED

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;     //전송 실패 횟수

    @Column(name = "last_error", length = LAST_ERROR_MAX)
    private String lastError;       //ㅑ마지막 에러 메시지 (최대 1000자)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;      //저장한 시각

    @Column(name = "published_at")
    private Instant publishedAt;        //KAFKA전송에 성공한 시각

    /**
     * 결제 완료 이벤트. payloadJson 필드(회원 ID, 호실 ID, 입주일 등)는 입주 상태 관리 담당과 협의 후 채운다.
     * 예: {"eventId":"...","applicationId":1}
     */
    //결제 확정과 같은 트렌젝션 안에서 작동
    public static OutboxEvent paymentCompleted(Long applicationId, String payloadJson) {
        OutboxEvent e = new OutboxEvent();
        //새 uuid 발급
        e.eventId = UUID.randomUUID();
        e.aggregateId = applicationId;
        e.eventType = PAYMENT_COMPLETED;
        e.payload = payloadJson;
        e.status = OutboxStatus.PENDING;
        return e;
        /*
        * 참고: payload에 무엇을 넣을지는 아직 협의
        * (주석 예시: {"eventId":"...","applicationId":1})
        * */
    }

    /** Kafka 발행 성공 (브로커 응답까지 받은 뒤) */
    public void markPublished() {
        if (this.status != OutboxStatus.PENDING) throw new IllegalStateException("PENDING 이벤트만 발행 완료 처리할 수 있습니다");
        this.status = OutboxStatus.PUBLISHED; //상태 저장
        this.publishedAt = Instant.now(); //시간 기록
    }

    /** 발행 실패: 재시도 횟수 증가, 한도를 넘으면 FAILED로 두고 관리자 확인 대상으로 남긴다 */
    public void recordFailure(String error, int maxRetry) {
        if (this.status != OutboxStatus.PENDING) throw new IllegalStateException("PENDING 이벤트만 실패를 기록할 수 있습니다");
        //시도횟수 카운트
        this.retryCount = this.retryCount + 1;
        //에러메시지 1000자로 자름
        this.lastError = (error == null || error.length() <= LAST_ERROR_MAX) ? error : error.substring(0, LAST_ERROR_MAX);
        //시행횟수의 리밋트를 정해놓음 maxRetry
        if (this.retryCount >= maxRetry) this.status = OutboxStatus.FAILED;
    }
}
