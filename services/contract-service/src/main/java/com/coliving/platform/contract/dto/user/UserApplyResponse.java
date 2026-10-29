package com.coliving.platform.contract.dto.user;

public record UserApplyResponse(
        Long userId,

        String name,
        String email,
        String phone,
        String address

) {
}
