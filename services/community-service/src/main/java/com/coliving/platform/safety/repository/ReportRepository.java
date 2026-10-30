package com.coliving.platform.safety.repository;

import com.coliving.platform.safety.entity.ReportEntity;
import com.coliving.platform.safety.entity.ReportStatus;
import com.coliving.platform.safety.entity.ReportTargetType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository extends JpaRepository<ReportEntity, Long>, JpaSpecificationExecutor<ReportEntity> {

	boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, String targetId);

	/** 같은 대상의 신고 전체 (최신순) */
	List<ReportEntity> findByTargetTypeAndTargetIdOrderByCreatedAtDescIdDesc(ReportTargetType targetType, String targetId);

	List<ReportEntity> findByTargetTypeAndTargetIdAndStatus(ReportTargetType targetType, String targetId, ReportStatus status);

	long countByTargetUserId(Long targetUserId);
}
