package com.coliving.reservation.controller;

import com.coliving.reservation.entity.ReservationExtension;
import com.coliving.reservation.service.ReservationExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation-service/admin/reservation-extensions")
public class AdminReservationExtensionController {

    private final ReservationExtensionService reservationExtensionService;

    // 관리자 예약 연장 신청 전체 조회
    @GetMapping
    public List<ReservationExtension> findAll() {
        return reservationExtensionService.findAll();
    }

    // 관리자 예약 연장 신청 상세 조회
    @GetMapping("/{id}")
    public ReservationExtension findById(@PathVariable Long id) {
        return reservationExtensionService.findById(id);
    }

    // 관리자 예약 연장 승인
    @PatchMapping("/{id}/approve")
    public ReservationExtension approveExtension(@PathVariable Long id) {
        return reservationExtensionService.approveExtension(id);
    }

    // 관리자 예약 연장 거절
    @PatchMapping("/{id}/reject")
    public ReservationExtension rejectExtension(@PathVariable Long id) {
        return reservationExtensionService.rejectExtension(id);
    }
}