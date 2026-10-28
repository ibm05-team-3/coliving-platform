package com.coliving.dashboard.roomstatus.dto;

import com.coliving.dashboard.roomstatus.document.RoomStatusDocument;
import com.coliving.dashboard.roomstatus.mapper.RoomStatusMapper;

/** 대시보드 화면의 "호실 현황" 영역에 내려주는 호실 한 칸의 응답. */
public record RoomStatusResponse(
        Long roomId,        // 호실 ID
        String roomName,    // 호실명
        Integer floor,      // 층
        String status       // OCCUPIED(입주) / VACANT(공실) / MOVING_OUT(퇴거 예정) / APPLYING(신청 진행 중)
) {
    /** ES 저장 데이터를 화면 응답으로 변환한다. */
    public static RoomStatusResponse from(RoomStatusDocument doc) {
        Integer floor = doc.getFloor() != null
                ? doc.getFloor()
                : RoomStatusMapper.guessFloor(doc.getRoomName());
        return new RoomStatusResponse(doc.getRoomId(), doc.getRoomName(), floor, doc.getStatus());
    }
}