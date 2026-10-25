package com.coliving.platform.contract.entity;

import com.coliving.platform.contract.enums.ReceiptType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * 결제 영수증 PDF 기록. 결제 1건에 결제 영수증(PAYMENT) 1개, 취소 시 취소 영수증(CANCEL) 1개.
 * (payment_id, receipt_type) UNIQUE는 DB가 막는다.
 */
@Entity
@Table(name = "payment_receipt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;        //자동채번 id

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;     //어느 결제의 영수증인지 저장

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_type", nullable = false, length = 20)
    private ReceiptType receiptType;    // PAYMENT(결제)/CANCEL(취소)

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;                            // 오브젝트 스토리지의 PDF경로

    @JdbcTypeCode(SqlTypes.CHAR)                        //string을 postSQL jsonB 컬럼에 넣기 위한 설정 CHAR(64)
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;     //PDF 내용의 SHA-256해시(64자).파일이 바뀌지 않았다는 증거에요.

    @Column(name = "emailed_at")
    private Instant emailedAt;                          //이메일 보낸시간 null이면 미발송 (재발송 대상)

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;       //영수증 발급 시각

    // issueAt은 자동으로 저장됨
    public static PaymentReceipt issue(Long paymentId, ReceiptType type, String filePath, String contentHash) {
        if (contentHash == null || contentHash.length() != 64)
            throw new IllegalArgumentException("영수증 해시는 SHA-256 hex 64자여야 합니다");
        PaymentReceipt r = new PaymentReceipt();
        r.paymentId = paymentId;
        r.receiptType = type;
        r.filePath = filePath;
        r.contentHash = contentHash;
        return r;
    }

    /** 이메일 발송 성공 후 호출 *///영수증 이메일발송이 성공했을경우 시간정보를 (비어있을때만)저장
    public void markEmailed() {
        if (this.emailedAt == null) this.emailedAt = Instant.now();
    }
}
