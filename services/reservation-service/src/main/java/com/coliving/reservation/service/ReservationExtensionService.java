
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

    // 전체 예약 연장 기록 조회
    public List<ReservationExtension> findAll() {
        return reservationExtensionRepository.findAll();
    }

    // 예약 연장 기록 상세 조회
    public ReservationExtension findById(Long id) {
        return reservationExtensionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "예약 연장 기록이 없습니다."
                ));
    }

    // 사용자 자동 연장 가능 여부 확인
    public boolean isExtensionAvailable(
            Long reservationId,
            LocalTime requestedEndTime) {

        ResourceReservation reservation =
                resourceReservationRepository.findById(reservationId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "예약 정보가 없습니다."
                        ));

        if (!"APPROVED".equals(reservation.getStatus())) {
            return false;
        }

        LocalDateTime reservationEndDateTime = LocalDateTime.of(
                reservation.getReservationDate(),
                reservation.getEndTime()
        );

        if (!LocalDateTime.now().isBefore(reservationEndDateTime)) {
            return false;
        }

        if (requestedEndTime == null ||
                !requestedEndTime.isAfter(reservation.getEndTime())) {
            return false;
        }

        long extensionMinutes = Duration.between(
                reservation.getEndTime(),
                requestedEndTime
        ).toMinutes();

        if (extensionMinutes != 60) {
            return false;
        }

        // 자동 연장은 예약당 최대 1회
        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservationId, "APPROVED")) {
            return false;
        }

        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservationId, "REQUESTED")) {
            return false;
        }

        SharedResource resource =
                sharedResourceRepository.findById(reservation.getResourceId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "공용 자원 정보가 없습니다."
                        ));

        if (!"AVAILABLE".equals(resource.getResourceStatus())) {
            return false;
        }

        if (resource.getOpeningTime() != null &&
                reservation.getStartTime().isBefore(resource.getOpeningTime())) {
            return false;
        }

        if (resource.getClosingTime() != null &&
                requestedEndTime.isAfter(resource.getClosingTime())) {
            return false;
        }

        long usageMinutes = Duration.between(
                reservation.getStartTime(),
                requestedEndTime
        ).toMinutes();

        long maxTotalMinutes =
                (resource.getMaxUsageHours() + 1L) * 60L;

        if (usageMinutes > maxTotalMinutes) {
            return false;
        }

        List<ResourceReservation> reservations =
                resourceReservationRepository.findByResourceIdAndReservationDate(
                        reservation.getResourceId(),
                        reservation.getReservationDate()
                );

        for (ResourceReservation other : reservations) {

            if (other.getId().equals(reservationId)) {
                continue;
            }

            if ("CANCELED".equals(other.getStatus())) {
                continue;
            }

            if (other.getStartTime().isBefore(requestedEndTime)
                    && other.getEndTime().isAfter(reservation.getEndTime())) {
                return false;
            }
        }

        return true;
    }

    // 사용자 자동 연장 및 자동 승인
    @Transactional
    public ReservationExtension saveExtension(
            Long reservationId,
            LocalTime requestedEndTime) {

        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservationId, "APPROVED")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 1회 연장한 예약입니다."
            );
        }

        if (reservationExtensionRepository.existsByReservationIdAndExtensionStatus(
                reservationId, "REQUESTED")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "기존 연장 신청이 대기 중입니다."
            );
        }

        if (!isExtensionAvailable(reservationId, requestedEndTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "예약 연장이 불가능합니다."
            );
        }

        ResourceReservation reservation =
                resourceReservationRepository.findById(reservationId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "예약 정보가 없습니다."
                        ));

        reservation.setEndTime(requestedEndTime);

        ReservationExtension extension =
                new ReservationExtension(reservationId, requestedEndTime);

        extension.changeStatus("APPROVED");

        resourceReservationRepository.save(reservation);

        return reservationExtensionRepository.save(extension);
    }

    // 관리자 추가 연장 처리
    @Transactional
    public ReservationExtension additionalExtension(
            Long reservationId,
            LocalTime requestedEndTime,
            Long adminId,
            String extensionReason) {

        // 필수값 확인
        if (reservationId == null ||
                requestedEndTime == null ||
                adminId == null ||
                extensionReason == null ||
                extensionReason.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "예약 ID, 종료 시간, 관리자 ID, 연장 사유는 필수입니다."
            );
        }

        String reason = extensionReason.trim();

        if (reason.length() > 500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "추가 연장 사유는 500자 이내로 입력해야 합니다."
            );
        }

        // 기존 예약 조회
        ResourceReservation reservation =
                resourceReservationRepository.findById(reservationId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "예약 정보가 없습니다."
                        ));

        // 승인된 예약만 추가 연장 가능
        if (!"APPROVED".equals(reservation.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "승인된 예약만 추가 연장할 수 있습니다."
            );
        }

        // 사용자 자동 연장 사용 여부 확인
        boolean autoExtended =
                reservationExtensionRepository.findAll().stream()
                        .anyMatch(extension ->
                                reservationId.equals(extension.getReservationId())
                                        && "AUTO".equals(extension.getExtensionType())
                                        && "APPROVED".equals(extension.getExtensionStatus())
                        );

        if (!autoExtended) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "자동 연장을 사용한 예약만 관리자 추가 연장이 가능합니다."
            );
        }

        // 종료된 예약은 추가 연장 불가능
        LocalDateTime reservationEndDateTime = LocalDateTime.of(
                reservation.getReservationDate(),
                reservation.getEndTime()
        );

        if (!LocalDateTime.now().isBefore(reservationEndDateTime)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미 종료된 예약은 추가 연장할 수 없습니다."
            );
        }

        // 정확히 1시간만 연장 가능
        if (!requestedEndTime.isAfter(reservation.getEndTime()) ||
                Duration.between(
                        reservation.getEndTime(),
                        requestedEndTime
                ).toMinutes() != 60) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "관리자 추가 연장은 정확히 1시간만 가능합니다."
            );
        }

        // 공용 자원 조회
        SharedResource resource =
                sharedResourceRepository.findById(reservation.getResourceId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "공용 자원 정보가 없습니다."
                        ));

        // 사용 중지된 자원은 추가 연장 불가능
        if (!"AVAILABLE".equals(resource.getResourceStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "사용 중지된 공용 자원은 추가 연장할 수 없습니다."
            );
        }

        // 운영 시간 확인
        if (resource.getOpeningTime() != null &&
                reservation.getStartTime().isBefore(resource.getOpeningTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "운영 시작 시간 이전의 예약은 연장할 수 없습니다."
            );
        }

        if (resource.getClosingTime() != null &&
                requestedEndTime.isAfter(resource.getClosingTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "운영 종료 시간을 초과할 수 없습니다."
            );
        }

        // 같은 자원, 같은 날짜의 예약 조회
        List<ResourceReservation> reservations =
                resourceReservationRepository.findByResourceIdAndReservationDate(
                        reservation.getResourceId(),
                        reservation.getReservationDate()
                );

        // 다른 예약과 시간 충돌 검사
        for (ResourceReservation other : reservations) {

            if (other.getId().equals(reservationId)) {
                continue;
            }

            if ("CANCELED".equals(other.getStatus())) {
                continue;
            }

            if (other.getStartTime().isBefore(requestedEndTime)
                    && other.getEndTime().isAfter(reservation.getEndTime())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "다음 예약과 시간이 겹쳐 추가 연장할 수 없습니다."
                );
            }
        }

        // 예약 종료 시간 변경
        reservation.setEndTime(requestedEndTime);

        // 관리자 추가 연장 기록 생성
        ReservationExtension extension = new ReservationExtension(
                reservationId,
                requestedEndTime,
                adminId,
                reason
        );

        // 예약 정보 저장
        resourceReservationRepository.save(reservation);

        // 관리자 추가 연장 기록 저장
        return reservationExtensionRepository.save(extension);
    }

    // 기존 관리자 승인 기능 차단
    @Transactional
    public ReservationExtension approveExtension(Long id) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "예약 연장은 자동 승인 방식으로 변경되었습니다."
        );
    }

    // 기존 관리자 거절 기능 차단
    @Transactional
    public ReservationExtension rejectExtension(Long id) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "예약 연장은 자동 승인 방식으로 변경되었습니다."
        );
    }
}
