
package com.coliving.reservation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ResourceReservationRequestDto {

    private Long resourceId;

    private Long userId;

    private LocalDate reservationDate;

    private LocalTime startTime;

    private LocalTime endTime;
}

