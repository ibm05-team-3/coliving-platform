package com.coliving.platform.contract.service;

import com.coliving.platform.contract.dto.internal.UserContractInfoDTO;
import com.coliving.platform.contract.dto.user.UserApplyResponse;

import org.springframework.stereotype.Service;

@Service
public class ApplyUserService {
    private  final UserQueryService userQueryService;

    public ApplyUserService(UserQueryService userQueryService){
        this.userQueryService = userQueryService;
    }
    public UserApplyResponse getUserForApply(Long userId){
        UserContractInfoDTO r = userQueryService.getUserForContract(userId);

        return new UserApplyResponse(
                r.userId(),
                r.name(),
                r.email(),
                r.phone(),
                r.address()
        );
    }

}
