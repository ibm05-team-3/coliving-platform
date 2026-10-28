package com.coliving.dashboard.kafka.consumer;

import com.coliving.dashboard.activitylog.service.ActivityLogService;
import com.coliving.dashboard.kafka.dto.ReservationStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 예약 상태 변경 이벤트를 받아 활동 로그에 전달한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationConsumer {

    private final JsonMapper jsonMapper;
    private final ActivityLogService activityLogService;

    /** JSON 문자열로 받아 이벤트 객체로 변환한다. 처리에 실패해도 로그만 남기고 다음 메시지로 넘어간다. */
    @KafkaListener(topics = "reservation.status-changed")
    public void consume(String message) {
        try {
            ReservationStatusChangedEvent event =
                    jsonMapper.readValue(message, ReservationStatusChangedEvent.class);
            activityLogService.saveReservationActivity(event);
        } catch (Exception e) {
            log.error("예약 상태 이벤트 처리 실패: {}", message, e);
        }
    }
}