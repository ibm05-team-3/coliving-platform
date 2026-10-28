package com.coliving.dashboard.roomstatus.mapper;

import java.util.Map;
import java.util.Optional;

/** 계약 상태를 호실 상태로 변환한다. */
public final class RoomStatusMapper {

    // 계약 상태 → 호실 상태
    private static final Map<String, String> MAPPING = Map.ofEntries(
            Map.entry("SUBMITTED", "APPLYING"),               // 신청 진행 중
            Map.entry("APPROVED", "APPLYING"),                // 신청 진행 중
            Map.entry("PAID", "APPLYING"),                    // 신청 진행 중
            Map.entry("COMPLETED", "OCCUPIED"),               // 입주
            Map.entry("TERMINATION_REQUESTED", "MOVING_OUT"), // 퇴거 예정
            Map.entry("TERMINATED", "MOVING_OUT"),            // 퇴거 예정
            Map.entry("REJECTED", "VACANT"),                  // 공실
            Map.entry("CANCELED", "VACANT"),                  // 공실
            Map.entry("EXPIRED", "VACANT"),                   // 공실
            Map.entry("REFUNDED", "VACANT")                   // 공실
    );

    private RoomStatusMapper() {}   // static 메서드만 쓰는 도구 클래스라 객체 생성을 막음

    /** 계약 상태에 해당하는 호실 상태를 반환한다. 매핑이 없으면 빈 값. */
    public static Optional<String> toRoomStatus(String contractStatus) {
        return Optional.ofNullable(MAPPING.get(contractStatus));
    }

    /** 호실명에서 끝 두 자리를 뺀 앞부분을 층으로 사용한다. */
    public static Integer guessFloor(String roomName) {
        if (roomName == null) return null;
        String digits = roomName.replaceAll("\\D", "");
        if (digits.length() < 3 || digits.length() > 6) return null;
        return Integer.parseInt(digits.substring(0, digits.length() - 2));
    }
}