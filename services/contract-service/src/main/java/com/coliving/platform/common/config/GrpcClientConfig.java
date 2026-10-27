package com.coliving.platform.common.config;

import com.coliving.platform.grpc.room.RoomServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

/**
 * gRPC 클라이언트(stub) 등록.
 *
 * Spring Boot 4.1부터는 stub을 자동으로 찾아 등록하지 않으므로 @ImportGrpcClients로 직접 등록한다.
 * target = "room" → application.yaml의 spring.grpc.client.channel.room.* 설정을 쓰는 채널에 연결된다.
 *
 * 등록되는 빈: RoomServiceGrpc.RoomServiceBlockingStub
 *   (Blocking = 응답이 올 때까지 기다리는 동기 호출. Spring MVC 서비스라 이게 맞다)
 */
@Configuration
@ImportGrpcClients(target = "room", types = RoomServiceGrpc.RoomServiceBlockingStub.class)
public class GrpcClientConfig {
}
