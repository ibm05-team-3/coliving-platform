package com.coliving.platform.contract.service;


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
}
