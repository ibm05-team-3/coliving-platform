package com.coliving.platform.contract.dto.internal;



/**
 * 유저 서비스(gRPC)에서 받은 유저 정보를 계약 서비스 안에서 쓰는 형태로 옮긴 DTO.
 *
 * gRPC가 만들어주는 클래스(RoomContractInfo)를 서비스·엔티티 곳곳에서 직접 쓰지 않고
 * UserQueryService에서 이 DTO로 바꿔서 넘긴다.
 * → 나중에 통신 방식이 또 바뀌어도 UserQueryService만 고치면 된다.
 */
public record UserContractInfoDTO(

        //식별
        Long userId,
        //화면 표시
        String name,
        String email,
        String phone,
        String address
) {
}
