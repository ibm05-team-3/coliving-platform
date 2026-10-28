package com.coliving.platform.note.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 2-1 비회원 문의 접수 */
public record GuestInquiryRequestDTO(
		@NotBlank(message = "이름을 입력해 주세요.")
		@Size(max = 50, message = "이름은 50자 이하로 입력해 주세요.")
		String name,

		@NotBlank(message = "휴대전화 번호를 입력해 주세요.")
		@Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "010-0000-0000 형식으로 입력해 주세요.")
		String phone,

		@NotBlank(message = "이메일을 입력해 주세요.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@NotBlank(message = "문의 내용을 입력해 주세요.")
		@Size(max = 1000, message = "1,000자 이하로 입력해 주세요.")
		String content,

		@NotNull(message = "개인정보 수집·이용 동의가 필요합니다.")
		Boolean privacyAgreed
) {
}
