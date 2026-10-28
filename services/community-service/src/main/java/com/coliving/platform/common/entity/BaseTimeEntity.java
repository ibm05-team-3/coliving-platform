package com.coliving.platform.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** 팀 공통: 생성·수정 일시 자동 기록 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

	@CreationTimestamp
	@Column(name = "created_at")
	protected LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	protected LocalDateTime updatedAt;
}
