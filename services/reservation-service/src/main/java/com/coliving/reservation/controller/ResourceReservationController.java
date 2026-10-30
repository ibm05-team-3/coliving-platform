
package com.coliving.reservation.controller;

import com.coliving.reservation.dto.ResourceReservationRequestDto;
import com.coliving.reservation.entity.ResourceReservation;
import com.coliving.reservation.service.ResourceReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation-service/reservations")
@RequiredArgsConstructor
public class ResourceReservationController {

    private final ResourceReservationService resourceReservationService;

    // 공용 자원 예약 신청
    @PostMapping
    public ResourceReservation createReservation(
            @RequestBody ResourceReservationRequestDto dto) {

        return resourceReservationService.createReservation(dto);
    }

    // 공용 물품 반납
    @PatchMapping("/{id}/return")
    public ResourceReservation returnItem(@PathVariable Long id) {

        return resourceReservationService.returnItem(id);
    }
}
