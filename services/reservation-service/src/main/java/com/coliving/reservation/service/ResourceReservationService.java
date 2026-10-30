
package com.coliving.reservation.service;

import com.coliving.reservation.entity.ResourceReservation;
import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.repository.ResourceReservationRepository;
import com.coliving.reservation.repository.SharedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResourceReservationService {

    private final ResourceReservationRepository resourceReservationRepository;
    private final SharedResourceRepository sharedResourceRepository;

    // 공용 물품 반납
    @Transactional
    public ResourceReservation returnItem(Long reservationId) {

        ResourceReservation reservation = resourceReservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        SharedResource resource = sharedResourceRepository.findById(reservation.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("공용 자원을 찾을 수 없습니다."));

        if (!"ITEM".equals(resource.getResourceType())) {
            throw new IllegalArgumentException("물품만 반납할 수 있습니다.");
        }

        if (!"APPROVED".equals(reservation.getStatus())
                && !"OVERDUE".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("반납할 수 없는 예약 상태입니다.");
        }

        if (reservation.getReturnedAt() != null) {
            throw new IllegalArgumentException("이미 반납된 물품입니다.");
        }

        reservation.setReturnedAt(LocalDateTime.now());
        reservation.setStatus("COMPLETED");

        return reservation;
    }
}
