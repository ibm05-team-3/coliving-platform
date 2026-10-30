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
import org.hibernate.annotations.ColumnTransformer;

/** 신고 — 관리자만 처리 (자동 숨김 없음), 신고자는 관리자에게만 공개 */
@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long reporterId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "report_source_type")
	private ReportSourceType sourceType;

	@Column(nullable = false)
	private Long sourceId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "report_target_type")
	private ReportTargetType targetType;

	@Column(nullable = false, length = 50)
	private String targetId;

	@Column(nullable = false)
	private Long targetUserId;

	@Column(nullable = false)
	private Long buildingId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "report_reason")
	private ReportReason reasonType;

	@Column(length = 500)
	private String reasonDetail;

	/** 신고 시점 내용 (JSON 문자열 → DB jsonb) */
	@Column(nullable = false, columnDefinition = "jsonb")
	@ColumnTransformer(write = "?::jsonb")
	private String contentSnapshot;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "report_status")
	private ReportStatus status;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(columnDefinition = "report_action")
	private ReportAction action;

	@Column(columnDefinition = "TEXT")
	private String adminMemo;

	private Long handledBy;

	private LocalDateTime handledAt;

	public static ReportEntity create(Long reporterId, ReportSourceType sourceType, Long sourceId,
			ReportTargetType targetType, String targetId, Long targetUserId, Long buildingId,
			ReportReason reasonType, String reasonDetail, String contentSnapshot) {
		ReportEntity report = new ReportEntity();
		report.reporterId = reporterId;
		report.sourceType = sourceType;
		report.sourceId = sourceId;
		report.targetType = targetType;
		report.targetId = targetId;
		report.targetUserId = targetUserId;
		report.buildingId = buildingId;
		report.reasonType = reasonType;
		report.reasonDetail = reasonDetail;
		report.contentSnapshot = contentSnapshot;
		report.status = ReportStatus.RECEIVED;
		return report;
	}

	public boolean isReceived() {
		return status == ReportStatus.RECEIVED;
	}

	/** 관리자 조치 기록 (반려면 REJECTED, 나머지는 RESOLVED) */
	public void handle(ReportAction action, String adminMemo, Long adminId, LocalDateTime handledAt) {
		this.status = action == ReportAction.REJECT ? ReportStatus.REJECTED : ReportStatus.RESOLVED;
		this.action = action;
		this.adminMemo = adminMemo;
		this.handledBy = adminId;
		this.handledAt = handledAt;
	}
}
