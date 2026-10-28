package com.coliving.platform.common.time;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/** 응답 일시를 한국 시간(+09:00)으로 맞추는 도우미 */
public final class KstTime {

	public static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private KstTime() {
	}

	public static OffsetDateTime now() {
		return OffsetDateTime.now(KST);
	}

	public static OffsetDateTime of(OffsetDateTime time) {
		return time == null ? null : time.atZoneSameInstant(KST).toOffsetDateTime();
	}
}
