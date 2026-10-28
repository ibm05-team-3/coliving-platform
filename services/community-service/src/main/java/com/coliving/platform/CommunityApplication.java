package com.coliving.platform;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {
		// 팀 규칙: 시간은 LocalDateTime(한국 시간) — 서버 시간대와 상관없이 한국 시간으로 기록
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(CommunityApplication.class, args);
	}

}
