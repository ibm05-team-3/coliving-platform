package com.coliving.platform.contract.service;


import com.coliving.platform.common.exception.UserNotFoundException;
import com.coliving.platform.common.exception.UserServiceException;
import com.coliving.platform.common.exception.UserServiceUnavailableException;
import com.coliving.platform.contract.dto.internal.UserContractInfoDTO;
import com.coliving.platform.grpc.user.GetUserContractInfoRequest;
import com.coliving.platform.grpc.user.UserContractInfo;
import com.coliving.platform.grpc.user.UserServiceGrpc;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Service;
/**
 * 계약 서비스 안에서 유저 정보가 필요할 때 거치는 창구 (유저 서비스 gRPC 호출).
 *
 * - gRPC 호출과 에러 변환을 이 클래스 한 곳에 모은다
 * - gRPC 생성 클래스(UserContractInfo)는 여기서만 쓰고, 밖으로는 UserContractInfoDTO로 넘긴다
 * -
 * - 응답 제한 시간은 application.yaml의 spring.grpc.client.channel.room.default.deadline
 */
@Service
public class UserQueryService {

    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserQueryService(UserServiceGrpc.UserServiceBlockingStub userStub) {
        this.userStub = userStub;
    }

    public UserContractInfoDTO getUserForContract(Long userId) {
        return toDto(call(userId));
    }

    // gRPC 호출 + 에러 변환
    private UserContractInfo call(Long userId) {
        GetUserContractInfoRequest request = GetUserContractInfoRequest.newBuilder()
                .setUserId(userId)
                .build();
        try {
            return userStub.getUserContractInfo(request);   // deadline 확인 결과에 따라 Room과 같이 맞출 것
        } catch (StatusRuntimeException e) {
            switch (e.getStatus().getCode()) {
                case NOT_FOUND ->
                        throw new UserNotFoundException("존재하지 않는 회원입니다.");
                case UNAVAILABLE, DEADLINE_EXCEEDED ->
                        throw new UserServiceUnavailableException("회원 서비스 응답 없음 (" + e.getStatus().getCode() + ")", e);
                default ->
                        throw new UserServiceException("회원 서비스 호출 실패 (" + e.getStatus().getCode() + ")", e);
            }
        }
    }

    // gRPC 응답 → DTO
    private UserContractInfoDTO toDto(UserContractInfo u) {
        return new UserContractInfoDTO(
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getAddress()
        );
    }
}
