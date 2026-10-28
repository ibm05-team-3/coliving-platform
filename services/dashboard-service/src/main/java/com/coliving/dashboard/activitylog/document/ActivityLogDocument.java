package com.coliving.dashboard.activitylog.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/** 최근 활동 로그. 같은 건의 상태가 바뀌면 같은 활동 로그 ID로 덮어쓴다. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "activity-log", createIndex = false, versionType = Document.VersionType.EXTERNAL)
public class ActivityLogDocument {

    @Id
    private String id;                       // 활동 로그 ID = {activity_type}_{source_ref_id} (예: CONTRACT_501)

    @Version
    private Long version;                    // 이벤트 발생 시간

    @Field(name = "event_id", type = FieldType.Keyword)
    private String eventId;                  // 마지막으로 반영한 이벤트 ID

    @Field(name = "building_id", type = FieldType.Long)
    private Long buildingId;                 // 건물 ID

    @Field(name = "building_name", type = FieldType.Keyword)
    private String buildingName;             // 건물명

    @Field(name = "activity_type", type = FieldType.Keyword)
    private String activityType;             // CONTRACT(계약) / RESERVATION(예약) / MOVE_IN(입주) / PAYMENT(결제)

    @Field(type = FieldType.Text)
    private String content;                  // 화면에 표시할 내용

    @Field(type = FieldType.Keyword)
    private String status;                   // COMPLETED(완료) / PENDING(대기) / PROCESSING(처리중)

    @Field(name = "source_service", type = FieldType.Keyword)
    private String sourceService;            // 이벤트를 보낸 서비스 (contract-service, amenity-service)

    @Field(name = "source_ref_id", type = FieldType.Keyword)
    private String sourceRefId;              // 원본 데이터 ID (계약 ID, 결제 ID, 예약 ID)

    @Field(name = "occurred_at", type = FieldType.Date, format = DateFormat.date_time)
    private Instant occurredAt;              // 최초 발생 시간

    @Field(name = "updated_at", type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;               // 최근 상태 변경 시간
}