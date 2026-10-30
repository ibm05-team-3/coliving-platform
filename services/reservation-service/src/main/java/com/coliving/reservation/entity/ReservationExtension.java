
package com.coliving.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "reservation_extensions")
public class ReservationExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연장 대상 예약 ID
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    // 연장 후 종료 시간
    @Column(name = "requested_end_time", nullable = false)
    private LocalTime requestedEndTime;

    // 연장 처리 상태
    @Column(name = "status", nullable = false)
    private String extensionStatus = "REQUESTED";

    // 연장 유형: AUTO 또는 EMERGENCY
    @Column(name = "extension_type")
    private String extensionType;

    // 긴급 연장을 처리한 관리자 ID
    @Column(name = "admin_id")
    private Long adminId;

    // 관리자 긴급 연장 사유
    @Column(name = "extension_reason", length = 500)
    private String extensionReason;

    // 연장 기록 생성 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기존 자동 연장 생성자
    public ReservationExtension(Long reservationId, LocalTime requestedEndTime) {
        this.reservationId = reservationId;
        this.requestedEndTime = requestedEndTime;
        this.extensionStatus = "REQUESTED";
        this.extensionType = "AUTO";
    }

    // 관리자 긴급 연장 생성자
    public ReservationExtension(
            Long reservationId,
            LocalTime requestedEndTime,
            Long adminId,
            String extensionReason) {

        this.reservationId = reservationId;
        this.requestedEndTime = requestedEndTime;
        this.extensionStatus = "APPROVED";
        this.extensionType = "EMERGENCY";
        this.adminId = adminId;
        this.extensionReason = extensionReason;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 예약 연장 상태 변경
    public void changeStatus(String status) {
        this.extensionStatus = status;
    }
}
