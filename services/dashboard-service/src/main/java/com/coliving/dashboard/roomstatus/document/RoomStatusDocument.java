package com.coliving.dashboard.roomstatus.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/** 호실 현황. 호실당 1건만 저장하고, 상태가 바뀌면 같은 저장 ID로 덮어쓴다. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "room-status", createIndex = false)
public class RoomStatusDocument {

    @Id
    private String id;                       // 호실 현황 ID = room_id

    @Field(name = "room_id", type = FieldType.Long)
    private Long roomId;                     // 호실 ID

    @Field(name = "building_id", type = FieldType.Long)
    private Long buildingId;                 // 건물 ID

    @Field(name = "building_name", type = FieldType.Keyword)
    private String buildingName;             // 건물명

    @Field(type = FieldType.Integer)
    private Integer floor;                   // 층

    @Field(name = "room_name", type = FieldType.Keyword)
    private String roomName;                 // 호실명

    @Field(type = FieldType.Keyword)
    private String status;                   // OCCUPIED(입주) / VACANT(공실) / MOVING_OUT(퇴거 예정) / APPLYING(신청 진행 중)

    @Field(name = "updated_at", type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;               // 상태 변경 시간
}