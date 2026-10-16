package com.coliving.platform.security.controller;

import com.coliving.platform.security.dto.RefreshRequestDTO;
import com.coliving.platform.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/user/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> jwtRefreshApi(@Validated @RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refreshRotate(dto));
    }

    @PostMapping(value = "/user/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logoutApi(@Validated @RequestBody RefreshRequestDTO dto) {
        authService.removeRefresh(dto.refreshToken());
        return ResponseEntity.ok().build();
    }
}
