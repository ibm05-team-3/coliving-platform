package com.coliving.dashboard.kafka.consumer;

import com.coliving.dashboard.activitylog.service.ActivityLogService;
import com.coliving.dashboard.kafka.dto.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** 결제 완료 이벤트를 받아 활동 로그에 전달한다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final JsonMapper jsonMapper;
    private final ActivityLogService activityLogService;

    /** JSON 문자열로 받아 이벤트 객체로 변환한다. 처리에 실패해도 로그만 남기고 다음 메시지로 넘어간다. */
    @KafkaListener(topics = "payment.completed")
    public void consume(String message) {
        try {
            PaymentCompletedEvent event =
                    jsonMapper.readValue(message, PaymentCompletedEvent.class);
            activityLogService.savePaymentActivity(event);
        } catch (Exception e) {
            log.error("결제 완료 이벤트 처리 실패: {}", message, e);
        }
    }
}