package com.coliving.reservation.scheduler;


import com.coliving.reservation.entity.ResourceReservation;
import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.repository.ResourceReservationRepository;
import com.coliving.reservation.repository.SharedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationExpirationScheduler {

    private final ResourceReservationRepository resourceReservationRepository;
    private final SharedResourceRepository sharedResourceRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireReservations() {

        // 1. 승인된 예약 목록 조회
        List<ResourceReservation> reservations =
                resourceReservationRepository.findByStatus("APPROVED");

        // 2. 현재 날짜와 시간
        LocalDateTime now = LocalDateTime.now();

        // 3. 예약 목록을 하나씩 확인
        for (ResourceReservation reservation : reservations) {

            // 4. 예약에 연결된 공용 자원 조회
            SharedResource resource = sharedResourceRepository
                    .findById(reservation.getResourceId())
                    .orElse(null);

            if (resource == null) {
                continue;
            }

            // 5. 공간형 공용 자원만 처리
            if (!"SPACE".equals(resource.getResourceType())) {
                continue;
            }

            // 6. 예약 종료 날짜와 시간
            LocalDateTime endDateTime = LocalDateTime.of(
                    reservation.getReservationDate(),
                    reservation.getEndTime()
            );

            // 7. 예약 종료 시간이 지났으면 만료 처리

            if (!now.isBefore(endDateTime)) {
                reservation.setStatus("EXPIRED");
            }
        }
    }
}
