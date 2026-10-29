
package com.coliving.reservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "shared_resources")
@Getter
@Setter
public class SharedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 건물 ID (추가)
    @Column(name = "building_id")
    private Long buildingId;

    // 공용 자원 이름
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    // 공용 자원 유형
    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType;

    // 공용 자원 설명
    @Column(name = "description")
    private String description;

    // 공용 자원 이용 상태
    @Column(name = "resource_status", nullable = false, length = 30)
    private String resourceStatus = "AVAILABLE";

    // 최대 이용 시간
    @Column(name = "max_usage_hours", nullable = false)
    private Integer maxUsageHours = 2;

    // 공용 자원 이미지 (추가)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 건물 내부 위치 (추가)
    @Column(name = "location", length = 100)
    private String location;

    // 최대 이용 인원 (추가)
    @Column(name = "capacity")
    private Integer capacity;

    // 운영 시작 시간 (추가)
    @Column(name = "opening_time")
    private LocalTime openingTime;

    // 운영 종료 시간 (추가)
    @Column(name = "closing_time")
    private LocalTime closingTime;

    // 생성일
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
