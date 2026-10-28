package com.coliving.platform.note.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 비회원 문의 — 연락처는 암호화된 값만 저장 */
@Entity
@Table(name = "guest_inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuestInquiryEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private String phoneEnc;

	@Column(nullable = false)
	private String emailEnc;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private LocalDateTime privacyAgreedAt;

	public static GuestInquiryEntity create(String name, String phoneEnc, String emailEnc, String content) {
		GuestInquiryEntity entity = new GuestInquiryEntity();
		entity.name = name;
		entity.phoneEnc = phoneEnc;
		entity.emailEnc = emailEnc;
		entity.content = content;
		entity.privacyAgreedAt = LocalDateTime.now();
		return entity;
	}
}
