package com.coliving.platform.safety.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 커뮤니티 제재 — 주거 계약과는 별개 */
@Entity
@Table(name = "user_sanctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSanctionEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long buildingId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "sanction_type")
	private SanctionType sanctionType;

	/** 이용 제한 기간 (1·3·7·30일, 경고·정지는 null) */
	@Column(columnDefinition = "smallint")
	private Integer durationDays;

	@Column(nullable = false, length = 500)
	private String reason;

	private Long reportId;

	@Column(nullable = false)
	private LocalDateTime startsAt;

	private LocalDateTime endsAt;

	@Column(nullable = false)
	private Long createdBy;

	private LocalDateTime revokedAt;

	private Long revokedBy;

	@Column(length = 500)
	private String revokeReason;

	public static UserSanctionEntity create(Long userId, Long buildingId, SanctionType type, Integer durationDays,
			String reason, Long reportId, Long createdBy, LocalDateTime now) {
		UserSanctionEntity sanction = new UserSanctionEntity();
		sanction.userId = userId;
		sanction.buildingId = buildingId;
		sanction.sanctionType = type;
		sanction.durationDays = type == SanctionType.RESTRICTION ? durationDays : null;
		sanction.reason = reason;
		sanction.reportId = reportId;
		sanction.createdBy = createdBy;
		sanction.startsAt = now;
		sanction.endsAt = type == SanctionType.RESTRICTION ? now.plusDays(durationDays) : null;
		return sanction;
	}

	/** 진행 중 = 이용 제한·정지 + 해제 안 됨 + 기간 남음 (경고는 항상 false) */
	public boolean isActive(LocalDateTime now) {
		if (sanctionType == SanctionType.WARNING || revokedAt != null) {
			return false;
		}
		return endsAt == null || endsAt.isAfter(now);
	}

	public void revoke(Long adminId, String reason, LocalDateTime now) {
		this.revokedAt = now;
		this.revokedBy = adminId;
		this.revokeReason = reason;
	}
}
