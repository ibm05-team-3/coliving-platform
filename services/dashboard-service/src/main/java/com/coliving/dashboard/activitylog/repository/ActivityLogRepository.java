package com.coliving.dashboard.activitylog.repository;

import com.coliving.dashboard.activitylog.document.ActivityLogDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/** 활동 로그 저장·조회. */
public interface ActivityLogRepository extends ElasticsearchRepository<ActivityLogDocument, String> {

    /** 건물별 활동 로그를 최근 변경 순으로 조회한다. */
    List<ActivityLogDocument> findByBuildingIdOrderByUpdatedAtDesc(Long buildingId, Pageable pageable);

    /** 건물별 활동 유형과 상태에 맞는 건수를 센다. */
    long countByBuildingIdAndActivityTypeAndStatus(Long buildingId, String activityType, String status);
}