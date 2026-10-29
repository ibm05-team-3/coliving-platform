package com.coliving.platform.contract.controller;


import com.coliving.platform.contract.dto.user.UserApplyResponse;
import com.coliving.platform.contract.service.ApplyUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController@RequestMapping("/internel")
public class ApplyUserController {

    private final ApplyUserService applyUserService;

    public ApplyUserController(ApplyUserService applyUserService){

        this.applyUserService = applyUserService;
    }

    @GetMapping("/user/{userId}")
    public UserApplyResponse getUser (@PathVariable Long userId){
        return applyUserService.getUserForApply(userId);
    }
}
