package com.coliving.dashboard.activitylog.controller;

import com.coliving.dashboard.activitylog.dto.ActivityLogResponse;
import com.coliving.dashboard.activitylog.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 대시보드 화면의 "최근 활동 로그" 영역에 데이터를 내려주는 API. */
@RestController
@RequestMapping("/dashboard/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    /** 건물별 최근 활동 로그 조회. */
    @GetMapping
    public List<ActivityLogResponse> recent(@RequestParam Long buildingId,
                                            @RequestParam(defaultValue = "10") int size) {
        return activityLogService.getRecent(buildingId, size);
    }
}