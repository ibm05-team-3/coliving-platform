package com.coliving.platform.contract.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contract")
public class ApplyRoomController {

    private final ApplyRoomservice applyRoomservice;
    public ApplyRoomController(ApplyRoomService applyRoomService){
        this.applyRoomservice = applyRoomService;
    }
    @GetMapping("/rooms/{roomId}")
    public RoomApplyResponse getRoom (@PathVariable Long roomId){}
    return applyRoomService.getRoomForApply(roomId);
}
