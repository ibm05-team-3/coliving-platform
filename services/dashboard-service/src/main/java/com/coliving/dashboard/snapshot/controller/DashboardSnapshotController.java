package com.coliving.dashboard.snapshot.controller;

import com.coliving.dashboard.snapshot.service.DashboardSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 일별 스냅샷을 수동으로 만드는 API. 매일 새벽 자동 생성을 기다리지 않고 바로 확인할 때 쓴다. */
@RestController
@RequestMapping("/dashboard/snapshots")
@RequiredArgsConstructor
public class DashboardSnapshotController {

    private final DashboardSnapshotService snapshotService;

    /** 모든 건물의 오늘 스냅샷을 지금 만든다. */
    @PostMapping("/refresh")
    public void refresh() {
        snapshotService.refreshAllToday();
    }
}
