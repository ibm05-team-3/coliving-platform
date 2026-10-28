package com.coliving.dashboard.roomstatus.controller;

import com.coliving.dashboard.roomstatus.dto.RoomStatusResponse;
import com.coliving.dashboard.roomstatus.service.RoomStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 대시보드 화면의 "호실 현황" 영역에 데이터를 내려주는 API. */
@RestController
@RequestMapping("/dashboard/rooms")
@RequiredArgsConstructor
public class RoomStatusController {

    private final RoomStatusService roomStatusService;

    /** 건물별 호실 현황 조회. */
    @GetMapping
    public List<RoomStatusResponse> rooms(@RequestParam Long buildingId) {
        return roomStatusService.getRooms(buildingId);
    }
}