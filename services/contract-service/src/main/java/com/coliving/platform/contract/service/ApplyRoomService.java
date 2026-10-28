package com.coliving.platform.contract.service;

import com.coliving.platform.contract.dto.internal.RoomContractInfoDTO;
import com.coliving.platform.contract.dto.room.RoomApplyResponse;
import org.springframework.stereotype.Service;

@Service
public class ApplyRoomService {
    private final RoomQueryService roomQueryService;


    public ApplyRoomService(RoomQueryService roomQueryService) {   // 클래스 이름과 같아야 함
        this.roomQueryService = roomQueryService;                  // 필드 이름과 똑같이
    }
    public RoomApplyResponse getRoomForApply(Long roomId) {
        RoomContractInfoDTO r = roomQueryService.getRoomForContract(roomId); // 삭제된 호실이면 여기서 예외

        return new RoomApplyResponse(
                r.roomId(),
                r.buildingName(),
                r.region(),
                r.roomName(),
                r.roomType(),
                r.thumbnailUrl(),
                r.roomFacilities(),
                r.area(),
                r.floorNumber(),
                r.monthlyFee(),
                r.deposit(),
                r.managementFee(),
                true                // available: 아래 "정해야 할 것" 참고
        );
    }

}
