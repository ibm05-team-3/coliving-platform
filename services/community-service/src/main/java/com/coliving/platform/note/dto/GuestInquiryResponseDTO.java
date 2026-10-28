package com.coliving.platform.note.dto;

import java.time.LocalDateTime;

/** 2-1 응답 */
public record GuestInquiryResponseDTO(Long inquiryId, LocalDateTime createdAt) {
}
