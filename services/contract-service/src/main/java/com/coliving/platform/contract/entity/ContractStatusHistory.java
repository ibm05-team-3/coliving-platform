package com.coliving.platform.contract.entity;

import com.coliving.platform.contract.enums.ChangedByType;
import com.coliving.platform.contract.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * 상태 이력. 상태가 바뀔 때마다 같은 트랜잭션에서 1행 INSERT하고, 절대 수정하지 않는다.
 * @Immutable: Hibernate가 이 엔티티에 UPDATE를 보내지 않는다.
 */
@Entity
@Immutable
@Table(name = "contract_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                //1씩 자동증가

    @Column(name = "application_id", nullable = false)
    private Long applicationId;                         // contract_application.id (객체 연관 없이 ID만)

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private ContractStatus fromStatus;                  //바뀌기 전상태, 첫 DRAFT 생성 때는 null

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private ContractStatus toStatus;        //바뀐 상태

    @Enumerated(EnumType.STRING)
    @Column(name = "changed_by_type", nullable = false, length = 10)
    private ChangedByType changedByType;        //누가바꿨는지

    @Column(name = "changed_by_id")
    private Long changedById;                           // 바꾼사람의 ID, SYSTEM(스케줄러)이면 null

    @Column(name = "reason", length = 500)
    private String reason;                              // 반려 사유 등

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;      //기록한 시각
    //private라서 라래의 생성자로만 채워진다.
    private ContractStatusHistory(Long applicationId, ContractStatus from, ContractStatus to,
                                  ChangedByType type, Long changedById, String reason) {
        this.applicationId = applicationId;
        this.fromStatus = from;
        this.toStatus = to;
        this.changedByType = type;
        this.changedById = changedById;
        this.reason = reason;
    }

    /** 사용자가 바꾼 경우 (DRAFT 생성, 제출, 취소 등). memberId는 JWT 값 */
    public static ContractStatusHistory user(Long applicationId, ContractStatus from, ContractStatus to, Long memberId) {
        return new ContractStatusHistory(applicationId, from, to, ChangedByType.USER, memberId, null);
    }

    /** 관리자가 바꾼 경우 (승인, 반려 + 사유) */
    public static ContractStatusHistory admin(Long applicationId, ContractStatus from, ContractStatus to,
                                              Long adminId, String reason) {
        return new ContractStatusHistory(applicationId, from, to, ChangedByType.ADMIN, adminId, reason);
    }

    /** 스케줄러·결제 확정 등 시스템이 바꾼 경우 (만료, PAID, COMPLETED) */
    public static ContractStatusHistory system(Long applicationId, ContractStatus from, ContractStatus to) {
        return new ContractStatusHistory(applicationId, from, to, ChangedByType.SYSTEM, null, null);
    }
}
