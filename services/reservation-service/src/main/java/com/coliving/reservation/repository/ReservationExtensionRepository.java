
package com.coliving.reservation.repository;

import com.coliving.reservation.entity.ReservationExtension;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationExtensionRepository
        extends JpaRepository<ReservationExtension, Long> {

    // 예약 연장 신청 상태 확인 (REQUESTED 또는 APPROVED)
    boolean existsByReservationIdAndExtensionStatus(
            Long reservationId,
            String extensionStatus
    );
}
