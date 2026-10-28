package com.coliving.platform.contract.service;

import com.coliving.platform.common.exception.RoomNotFoundException;
import com.coliving.platform.common.exception.RoomServiceUnavailableException;
import com.coliving.platform.contract.dto.internal.RoomContractInfoDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * [개발용 더미] 매물 서비스(gRPC)가 아직 없을 때 쓰는 가짜 호실 조회.
 * - "dummy-room" 프로필을 켰을 때만 빈으로 등록된다 (안 켜면 진짜 RoomQueryService가 동작)
 * - @Primary라서 RoomQueryService를 주입받는 곳(ApplyRoomService 등)에 이 클래스가 대신 들어간다
 * - gRPC를 실제로 호출하지 않으므로 매물 서비스 없이 Postman 테스트 가능
 * 테스트용 roomId
 *   1   : 정상 호실 (사진 없음 → thumbnailUrl null)
 *   2   : 정상 호실 (사진 2장)
 *   3   : 매물 쪽에서 삭제된 호실 → 404
 *   503 : 매물 서비스 꺼짐 흉내 → 503
 *   그 외 : 없는 호실 → 404
 * ⚠ 매물 서비스가 준비되면 프로필만 끄면 된다. 운영 설정에 dummy-room을 넣지 말 것.
 */
@Service
@Primary
@Profile("dummy-room")
public class DummyRoomQueryService extends RoomQueryService {

    private static final Map<Long, RoomContractInfoDTO> ROOMS = Map.of(
            1L, room(1L, List.of(), "302호", "1인실",
                    List.of("전용 욕실"), "13.50", 3, 780_000L, 1_000_000L, 50_000L),
            2L, room(2L,
                    List.of("https://picsum.photos/seed/room2a/800/600", "https://picsum.photos/seed/room2b/800/600"),
                    "501호", "2인실",
                    List.of("전용 욕실", "발코니"), "21.00", 5, 1_150_000L, 2_000_000L, 70_000L)
    );

    public DummyRoomQueryService() {
        // 부모는 stub을 저장만 하고, getRoomForContract를 아래에서 덮어써서 stub을 쓰지 않는다
        // → stub 빈 없이도 동작 (매물 gRPC 설정이 없어도 됨)
        super(null);
    }

    @Override
    public RoomContractInfoDTO getRoomForContract(Long roomId) {
        if (roomId == 503L) {
            throw new RoomServiceUnavailableException("[더미] 매물 서비스 응답 없음", null);
        }
        if (roomId == 3L) {
            throw new RoomNotFoundException("[더미] 더 이상 계약할 수 없는 호실입니다.");
        }
        RoomContractInfoDTO r = ROOMS.get(roomId);
        if (r == null) {
            throw new RoomNotFoundException("[더미] 호실을 찾을 수 없습니다: " + roomId);
        }
        return r;
    }

    // 더미 호실은 모두 같은 건물 소속
    private static RoomContractInfoDTO room(Long roomId, List<String> imageUrls, String roomName, String roomType,
                                            List<String> roomFacilities, String area, int floor,
                                            long monthlyFee, long deposit, long managementFee) {
        return new RoomContractInfoDTO(
                roomId,
                10L,                          // buildingId
                imageUrls,
                "포레스트 153",                // buildingName
                "서울시 성동구 성수이로 153",   // address
                "서울 성동구",                  // region
                roomName,
                roomType,
                roomFacilities,
                List.of("공용 라운지", "세탁실"),  // buildingFacilities
                new BigDecimal(area),
                floor,
                monthlyFee,
                deposit,
                managementFee,
                "MONTHLY",                    // rentalType
                1,                            // capacity
                null,                         // roomStructure
                false                         // deleted
        );
    }
}
