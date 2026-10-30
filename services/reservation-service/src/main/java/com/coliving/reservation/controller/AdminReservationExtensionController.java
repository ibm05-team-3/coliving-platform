
package com.coliving.reservation.controller;

import com.coliving.reservation.dto.AdminAdditionalExtensionRequestDto;
import com.coliving.reservation.entity.ReservationExtension;
import com.coliving.reservation.service.ReservationExtensionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation-service/admin/reservation-extensions")
public class AdminReservationExtensionController {

    private final ReservationExtensionService reservationExtensionService;

    // 1. 관리자 예약 연장 기록 전체 조회
    @GetMapping
    public List<ReservationExtension> findAll() {
        return reservationExtensionService.findAll();
    }

    // 2. 관리자 예약 연장 기록 상세 조회
    @GetMapping("/{id}")
    public ReservationExtension findById(@PathVariable Long id) {
        return reservationExtensionService.findById(id);
    }

    // 3. 관리자 추가 연장
    @PostMapping("/additional")
    public ReservationExtension additionalExtension(
            @RequestBody AdminAdditionalExtensionRequestDto dto,
            Principal principal) {

        // 관리자 로그인 여부 확인
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "관리자 로그인이 필요합니다."
            );
        }

        // 인증된 관리자 ID 가져오기
        Long adminId;

        try {
            adminId = Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "관리자 ID를 확인할 수 없습니다."
            );
        }

        // 관리자 추가 연장 처리
        return reservationExtensionService.additionalExtension(
                dto.getReservationId(),
                dto.getRequestedEndTime(),
                adminId,
                dto.getExtensionReason()
        );
    }
}
