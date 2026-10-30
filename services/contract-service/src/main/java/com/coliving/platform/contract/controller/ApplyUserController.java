package com.coliving.platform.contract.controller;


import com.coliving.platform.contract.dto.user.UserApplyResponse;
import com.coliving.platform.contract.service.ApplyUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: gRPC 연결 테스트용 임시 API. 다른 서비스가 부르는 API가 아니라서 원래 internal 규칙에도 맞지 않는다.
 *  - userId를 URL로 받으면 숫자만 바꿔서 남의 이름·전화번호·주소를 볼 수 있다 → 프론트에 연결하지 말 것
 *  - GET /apply/applications/{applicationNo}/form 완성 후 삭제 (userId는 gateway가 넣어주는 X-User 헤더에서 꺼냄)
 */
@RestController
@RequestMapping("/internal/apply")
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
