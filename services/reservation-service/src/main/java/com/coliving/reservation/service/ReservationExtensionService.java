
package com.coliving.reservation.service;

import com.coliving.reservation.entity.ReservationExtension;
import com.coliving.reservation.entity.ResourceReservation;
import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.repository.ReservationExtensionRepository;
import com.coliving.reservation.repository.ResourceReservationRepository;
import com.coliving.reservation.repository.SharedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationExtensionService {

    private final ReservationExtensionRepository reservationExtensionRepository;
    private final ResourceReservationRepository resourceReservationRepository;
    private final SharedResourceRepository sharedResourceRepository;

    // 전체 예약 연장 신청 조회
    public List<ReservationExtension> findAll() {
        return reservationExtensionRepository.findAll();
    }

    // 예약 연장 신청 상세 조회
    public ReservationExtension findById(Long id) {
        return reservationExtensionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "예약 연장 신청이 없습니다."
                ));
    }

    // 예약 연장 시간 중복 및 최대 이용 시간 확인
    public boolean isExtensionAvailable(Long reservationId, LocalTime requestedEndTime) {

        // 현재 예약 조회
        ResourceReservation reservation = resourceReservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "예약 정보가 없습니다."
                ));

        // 승인된 예약만 연장 가능
        if (!"APPROVED".equals(reservation.getStatus())) {
            return false;
        }

        // 예약 종료 날짜와 시간을 합치기
        LocalDateTime reservationEndDateTime = LocalDateTime.of(
                reservation.getReservationDate(),
                reservation.getEndTime()
        );

        // 현재 시간이 예약 종료 시간과 같거나 지났으면 연장 불가능
        if (!LocalDateTime.now().isBefore(reservationEndDateTime)) {
            return false;
        }

        // 연장 종료 시간이 현재 종료 시간보다 늦어야 함
        if (requestedEndTime == null ||
                !requestedEndTime.isAfter(reservation.getEndTime())) {
            return false;
        }

        // 현재 예약 종료 시간부터 요청한 종료 시간까지 연장 시간 계산
        long extensionMinutes = Duration.between(
                reservation.getEndTime(),
                requestedEndTime
        ).toMinutes();

        // 한 번에 최대 1시간까지만 연장 가능
        if (extensionMinutes > 60) {
            return false;
        }

        // 공용 자원 조회
        SharedResource resource = sharedResourceRepository.findById(reservation.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "공용 자원 정보가 없습니다."
                ));

        // 공용 자원 운영 시작 시간 확인
        if (resource.getOpeningTime() != null &&
                reservation.getStartTime().isBefore(resource.getOpeningTime())) {
            return false;
        }

        // 공용 자원 운영 종료 시간 확인
        if (resource.getClosingTime() != null &&
                requestedEndTime.isAfter(resource.getClosingTime())) {
            return false;
        }

        // 예약 시작부터 연장 종료까지 총 이용 시간 계산
        long usageMinutes = Duration.between(
                reservation.getStartTime(),
                requestedEndTime
        ).toMinutes();

        // 기본 최대 이용 시간에 연장 가능 시간 1시간 추가
        long maxTotalMinutes = (resource.getMaxUsageHours() + 1L) * 60L;

        // 연장을 포함한 총 이용 시간이 최대 시간을 초과하면 불가능
        if (usageMinutes > maxTotalMinutes) {
            return false;
        }

        // 같은 공용 자원, 같은 날짜의 예약 조회
        List<ResourceReservation> reservations =
                resourceReservationRepository.findByResourceIdAndReservationDate(
                        reservation.getResourceId(),
                        reservation.getReservationDate()
                );

        // 다른 예약과 시간이 겹치는지 확인
        for (ResourceReservation other : reservations) {

            // 자기 자신의 예약은 제외
            if (other.getId().equals(reservationId)) {
                continue;
            }

            // 취소된 예약은 제외
            if ("CANCELLED".equals(other.getStatus())) {
                continue;
            }

            // 연장 시간과 다른 예약 시간이 겹치면 연장 불가능
            if (other.getStartTime().isBefore(requestedEndTime)
                    && other.getEndTime().isAfter(reservation.getEndTime())) {
                return false;
            }
        }

        // 모든 조건을 만족하면 연장 가능
        return true;
    }

    // 예약 연장 신청 저장
    public ReservationExtension saveExtension(Long reservationId, LocalTime requestedEndTime) {

        // 이미 승인 대기 중인 연장 신청이 있는지 확인
        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservationId, "REQUESTED")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 예약 연장 신청이 존재합니다."
            );
        }

        // 이미 승인된 연장 신청이 있는지 확인
        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservationId, "APPROVED")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 연장이 승인된 예약입니다."
            );
        }

        // 예약 연장이 가능한지 확인
        if (!isExtensionAvailable(reservationId, requestedEndTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "예약 연장이 불가능합니다."
            );
        }

        // 새로운 예약 연장 신청 객체 생성
        ReservationExtension extension =
                new ReservationExtension(reservationId, requestedEndTime);

        // DB에 예약 연장 신청 저장
        return reservationExtensionRepository.save(extension);
    }

    // 관리자 예약 연장 승인
    @Transactional
    public ReservationExtension approveExtension(Long id) {

        // 예약 연장 신청 조회
        ReservationExtension extension = findById(id);

        // 이미 처리된 신청인지 확인
        if (!"REQUESTED".equals(extension.getExtensionStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 처리된 예약 연장 신청입니다."
            );
        }

        // 기존 예약 조회
        ResourceReservation reservation =
                resourceReservationRepository.findById(extension.getReservationId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "예약 정보가 없습니다."
                        ));

        // 이미 승인된 연장 신청이 있는지 확인
        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservation.getId(), "APPROVED")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 연장이 승인된 예약입니다."
            );
        }

        // 승인 시점에 예약 연장 가능 여부 다시 확인
        if (!isExtensionAvailable(
                reservation.getId(),
                extension.getRequestedEndTime())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "예약 연장이 불가능합니다."
            );
        }

        // 기존 예약 종료 시간 변경
        reservation.setEndTime(extension.getRequestedEndTime());

        // 예약 연장 신청 상태를 승인으로 변경
        extension.changeStatus("APPROVED");

        // 변경된 예약 정보 저장
        resourceReservationRepository.save(reservation);

        // 변경된 연장 신청 정보 저장
        return reservationExtensionRepository.save(extension);
    }

    // 관리자 예약 연장 거절
    @Transactional
    public ReservationExtension rejectExtension(Long id) {

        // 예약 연장 신청 조회
        ReservationExtension extension = findById(id);

        // 이미 승인 또는 거절된 신청인지 확인
        if (!"REQUESTED".equals(extension.getExtensionStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 처리된 예약 연장 신청입니다."
            );
        }

        // 예약 연장 상태를 거절로 변경
        extension.changeStatus("REJECTED");

        // 변경된 상태를 DB에 저장
        return reservationExtensionRepository.save(extension);
    }
}
