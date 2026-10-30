package com.coliving.dashboard.kafka.consumer;

import com.coliving.dashboard.activitylog.service.ActivityLogService;
import com.coliving.dashboard.kafka.dto.ContractStatusChangedEvent;
import com.coliving.dashboard.roomstatus.service.RoomStatusService;
import com.coliving.dashboard.snapshot.service.DashboardSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 계약 상태 변경 이벤트를 받아 호실 현황과 활동 로그에 나눠 전달한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractStatusConsumer {

    private final JsonMapper jsonMapper;
    private final RoomStatusService roomStatusService;
    private final ActivityLogService activityLogService;
    private final DashboardSnapshotService snapshotService;


    /** JSON 문자열로 받아 이벤트 객체로 변환한다. 처리에 실패해도 로그만 남기고 다음 메시지로 넘어간다. */
    @KafkaListener(topics = "contract.status-changed")
    public void consume(String message) {
        try {
            ContractStatusChangedEvent event =
                    jsonMapper.readValue(message, ContractStatusChangedEvent.class);
            roomStatusService.applyContractEvent(event);       // 호실 현황 갱신
            activityLogService.saveContractActivity(event);    // 활동 로그 저장
            snapshotService.refreshToday(event.buildingId(), event.buildingName());
        } catch (Exception e) {
            log.error("계약 상태 이벤트 처리 실패: {}", message, e);
        }
    }
}