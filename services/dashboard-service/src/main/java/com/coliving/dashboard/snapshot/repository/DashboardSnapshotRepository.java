package com.coliving.dashboard.snapshot.repository;

import com.coliving.dashboard.snapshot.document.DashboardSnapshotDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDate;
import java.util.Optional;

/** 일별 집계 스냅샷 저장·조회. */
public interface DashboardSnapshotRepository
        extends ElasticsearchRepository<DashboardSnapshotDocument, String> {

    /** 건물의 기준일 이전 스냅샷 중 가장 최근 1건을 조회한다. */
    Optional<DashboardSnapshotDocument>
    findFirstByBuildingIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
            Long buildingId, LocalDate date);
}