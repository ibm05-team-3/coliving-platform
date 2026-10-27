package com.coliving.platform.contract.service;

import com.coliving.platform.common.exception.RoomNotFoundException;
import com.coliving.platform.common.exception.RoomServiceException;
import com.coliving.platform.common.exception.RoomServiceUnavailableException;
import com.coliving.platform.grpc.room.GetRoomContractInfoRequest;
import com.coliving.platform.grpc.room.RoomContractInfo;
import com.coliving.platform.grpc.room.RoomServiceGrpc;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 계약 서비스 안에서 호실 정보가 필요할 때 거치는 창구 (매물 서비스 gRPC 호출).
 *
 * - gRPC 호출과 에러 변환을 이 클래스 한 곳에 모은다
 * - gRPC 생성 클래스(RoomContractInfo)는 여기서만 쓰고, 밖으로는 RoomContractInfoDTO로 넘긴다
 * - 회원/비회원 구분 없이 호출 가능 (사용자 정보 불필요)
 * - 응답 제한 시간은 application.yaml의 spring.grpc.client.channel.room.default.deadline
 */
@Service
public class RoomQueryService {

    private final RoomServiceGrpc.RoomServiceBlockingStub roomStub;

    public RoomQueryService(RoomServiceGrpc.RoomServiceBlockingStub roomStub) {
        this.roomStub = roomStub;
    }

    public RoomContractInfoDTO getRoomForContract(Long roomId) {
        RoomContractInfo res = call(roomId);

        // 매물 쪽에서 소프트 삭제된 호실은 계약 신청 불가
        if (res.getDeleted()) {
            throw new RoomNotFoundException("더 이상 계약할 수 없는 호실입니다.");
        }
        return toDto(res);
    }

    // ---------------------------------------------------------------
    // gRPC 호출 + 에러 변환
    // REST(Feign)는 HTTP 상태코드(404, 500)로 에러가 왔지만,
    // gRPC는 StatusRuntimeException 하나로 오고 그 안의 Status 코드로 구분한다.
    // ---------------------------------------------------------------
    private RoomContractInfo call(Long roomId) {
        GetRoomContractInfoRequest request = GetRoomContractInfoRequest.newBuilder()
                .setRoomId(roomId)
                .build();
        try {
            return roomStub.getRoomContractInfo(request);
        } catch (StatusRuntimeException e) {
            switch (e.getStatus().getCode()) {
                case NOT_FOUND ->
                        throw new RoomNotFoundException("존재하지 않는 호실입니다.");
                case UNAVAILABLE, DEADLINE_EXCEEDED ->   // 서버 꺼짐 / 응답 시간 초과
                        throw new RoomServiceUnavailableException("매물 서비스 응답 없음 (" + e.getStatus().getCode() + ")", e);
                default ->
                        throw new RoomServiceException("매물 서비스 호출 실패 (" + e.getStatus().getCode() + ")", e);
            }
        }
    }

    // ---------------------------------------------------------------
    // gRPC 응답 → DTO
    // proto3는 값이 없으면 null이 아니라 기본값("" , 0, 빈 리스트)을 준다.
    // NULL이 의미 있는 필드(room_structure)만 optional로 정의해서 has...()로 구분한다.
    // ---------------------------------------------------------------
    private RoomContractInfoDTO toDto(RoomContractInfo r) {
        return new RoomContractInfoDTO(
                r.getRoomId(),
                r.getBuildingId(),
                List.copyOf(r.getImageUrlsList()),
                r.getBuildingName(),
                r.getAddress(),
                r.getRegion(),
                r.getRoomName(),
                r.getRoomType(),
                List.copyOf(r.getRoomFacilitiesList()),
                List.copyOf(r.getBuildingFacilitiesList()),
                toArea(r.getArea()),
                r.getFloorNumber(),
                r.getMonthlyFee(),
                r.getDeposit(),
                r.getManagementFee(),
                r.getRentalType(),
                r.getCapacity(),
                r.hasRoomStructure() ? r.getRoomStructure() : null,
                r.getDeleted()
        );
    }

    private BigDecimal toArea(String area) {
        if (area == null || area.isBlank()) {
            throw new RoomServiceException("매물 서비스 응답에 면적 값이 없습니다.", null);
        }
        try {
            return new BigDecimal(area);
        } catch (NumberFormatException e) {
            throw new RoomServiceException("면적 형식이 올바르지 않습니다: " + area, e);
        }
    }
}
