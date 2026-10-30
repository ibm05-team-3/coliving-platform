
package com.coliving.reservation.service;

import com.coliving.reservation.dto.ResourceReservationRequestDto;
import com.coliving.reservation.entity.ResourceReservation;
import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.repository.ResourceReservationRepository;
import com.coliving.reservation.repository.SharedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    // 공용 자원 예약 신청
    @Transactional
    public ResourceReservation createReservation(ResourceReservationRequestDto dto) {

        // 예약 정보 입력 확인
        if (dto == null
                || dto.getResourceId() == null
                || dto.getUserId() == null
                || dto.getReservationDate() == null
                || dto.getStartTime() == null
                || dto.getEndTime() == null) {
            throw new IllegalArgumentException("예약 정보를 모두 입력해야 합니다.");
        }

        // 공용 자원 조회
        SharedResource resource = sharedResourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("공용 자원을 찾을 수 없습니다."));

        // 예약 가능한 공용 자원인지 확인
        if (!"AVAILABLE".equals(resource.getResourceStatus())) {
            throw new IllegalArgumentException("현재 예약할 수 없는 공용 자원입니다.");
        }

        // 최초 예약은 1시간 또는 2시간만 가능
        long minutes = Duration.between(
                dto.getStartTime(),
                dto.getEndTime()
        ).toMinutes();

        if (minutes != 60 && minutes != 120) {
            throw new IllegalArgumentException("최초 예약은 1시간 또는 2시간만 가능합니다.");
        }

        // 과거 시간 예약 차단
        LocalDateTime startDateTime = LocalDateTime.of(
                dto.getReservationDate(),
                dto.getStartTime()
        );

        if (!startDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 시간 이후로만 예약할 수 있습니다.");
        }

        // 반납하지 않은 연체 물품인지 확인
        if ("ITEM".equals(resource.getResourceType())) {

            boolean hasOverdue = resourceReservationRepository
                    .existsByResourceIdAndReturnedAtIsNullAndStatusIn(
                            dto.getResourceId(),
                            List.of("OVERDUE")
                    );

            if (hasOverdue) {
                throw new IllegalArgumentException("미반납된 물품이므로 예약할 수 없습니다.");
            }
        }

        // 같은 날짜에 해당 자원의 기존 예약 조회
        List<ResourceReservation> reservations =
                resourceReservationRepository.findByResourceIdAndReservationDate(
                        dto.getResourceId(),
                        dto.getReservationDate()
                );

        // 기존 예약과 시간이 겹치는지 확인
        for (ResourceReservation existing : reservations) {

            // 예약이 승인되었거나 연체 중인 경우 검사
            if (!"APPROVED".equals(existing.getStatus())
                    && !"OVERDUE".equals(existing.getStatus())) {
                continue;
            }

            boolean isOverlapping =
                    dto.getStartTime().isBefore(existing.getEndTime())
                            && dto.getEndTime().isAfter(existing.getStartTime());

            if (isOverlapping) {
                throw new IllegalArgumentException("이미 예약된 시간입니다.");
            }
        }

        // 예약 정보 생성
        ResourceReservation reservation = new ResourceReservation();

        // 예약번호 자동 생성
        reservation.setReservationNumber(
                "RES-" + UUID.randomUUID().toString().substring(0, 8)
        );

        // 예약 정보 저장
        reservation.setResourceId(dto.getResourceId());
        reservation.setUserId(dto.getUserId());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());

        // 관리자 승인 없이 자동 승인
        reservation.setStatus("APPROVED");

        // PostgreSQL에 예약 저장
        return resourceReservationRepository.save(reservation);
    }
}
