
package com.coliving.reservation.controller;

import com.coliving.reservation.dto.ReservationExtensionRequestDto;
import com.coliving.reservation.entity.ReservationExtension;
import com.coliving.reservation.service.ReservationExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation-service/reservation-extensions")
public class ReservationExtensionController {

    private final ReservationExtensionService reservationExtensionService;

    // 사용자 예약 연장 신청
    @PostMapping
    public ReservationExtension saveExtension(
            @RequestBody ReservationExtensionRequestDto dto) {

        return reservationExtensionService.saveExtension(
                dto.getReservationId(),
                dto.getRequestedEndTime()
        );
    }
}
