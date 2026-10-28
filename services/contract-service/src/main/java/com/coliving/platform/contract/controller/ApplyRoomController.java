package com.coliving.platform.contract.controller;


import com.coliving.platform.contract.dto.room.RoomApplyResponse;
import com.coliving.platform.contract.service.ApplyRoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internel/apply/")
public class ApplyRoomController {

    private final ApplyRoomService applyRoomService;

    public ApplyRoomController(ApplyRoomService applyRoomService) {
        this.applyRoomService = applyRoomService;
    }
        @GetMapping("/room/{roomId}")
        public RoomApplyResponse getRoom (@PathVariable Long roomId){

            return applyRoomService.getRoomForApply(roomId);
        }
/*
* POST  /apply/applications                      ← 생성 (번호 없음)
PATCH /apply/applications/{applicationNo}      ← 이전으로 돌아와서 수정
GET   /apply/applications/{applicationNo}/form ← 신청자 정보 화면*/
}
