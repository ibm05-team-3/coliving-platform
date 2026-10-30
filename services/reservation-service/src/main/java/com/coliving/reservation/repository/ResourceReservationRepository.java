package com.coliving.reservation.repository;

import com.coliving.reservation.entity.ResourceReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ResourceReservationRepository
        extends JpaRepository<ResourceReservation, Long> {

    List<ResourceReservation> findByResourceIdAndReservationDate(
            Long resourceId,
            LocalDate reservationDate
    );

    List<ResourceReservation> findByStatus(String status);

    // 반납하지 않은 물품 예약 존재 여부 확인
    boolean existsByResourceIdAndReturnedAtIsNullAndStatusIn(
            Long resourceId,
            List<String> statuses
    );
}