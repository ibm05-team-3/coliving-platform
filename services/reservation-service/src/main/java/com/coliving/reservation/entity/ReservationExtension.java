
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

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "requested_end_time", nullable = false)
    private LocalTime requestedEndTime;

    @Column(name = "status", nullable = false)
    private String extensionStatus = "REQUESTED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 예약 연장 신청 객체 생성
    public ReservationExtension(Long reservationId, LocalTime requestedEndTime) {
        this.reservationId = reservationId;
        this.requestedEndTime = requestedEndTime;
        this.extensionStatus = "REQUESTED";
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 예약 연장 신청 상태 변경
    public void changeStatus(String status) {
        this.extensionStatus = status;
    }
}



