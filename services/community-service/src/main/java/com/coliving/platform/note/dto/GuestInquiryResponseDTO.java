package com.coliving.platform.note.dto;

import java.time.OffsetDateTime;

/** 2-1 응답 */
public record GuestInquiryResponseDTO(Long inquiryId, OffsetDateTime createdAt) {
}
