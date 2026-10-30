package com.coliving.platform.contract.dummy;

import com.coliving.platform.common.exception.UserNotFoundException;
import com.coliving.platform.common.exception.UserServiceUnavailableException;
import com.coliving.platform.contract.dto.internal.UserContractInfoDTO;
import com.coliving.platform.contract.service.UserQueryService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("dummy-user")
public class DummyUserQueryService extends UserQueryService {

    public DummyUserQueryService() { super(null); }   // stub 안 씀

    @Override
    public UserContractInfoDTO getUserForContract(Long userId) {
        if (userId == 503L) throw new UserServiceUnavailableException("[더미] 회원 서비스 응답 없음", null);
        return switch (userId.intValue()) {
            case 1 -> new UserContractInfoDTO(1L, "홍길동", "hong@test.com", "010-1234-5678", "서울시 성동구");
            case 2 -> new UserContractInfoDTO(2L, "김주소없음", "kim@test.com", "", "");  // proto3 기본값 흉내
            default -> throw new UserNotFoundException("[더미] 존재하지 않는 회원입니다.");
        };
    }
}