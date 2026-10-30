
package com.coliving.reservation.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class AdminAdditionalExtensionRequestDto {

    private Long reservationId;

    private LocalTime requestedEndTime;

    private String extensionReason;
}
