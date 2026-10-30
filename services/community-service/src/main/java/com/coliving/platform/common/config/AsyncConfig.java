package com.coliving.platform.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** @Async 사용 — 실시간 전송·알림 저장을 요청 처리와 분리 (Spring Boot 기본 스레드 풀 사용) */
@Configuration
@EnableAsync
public class AsyncConfig {
}
