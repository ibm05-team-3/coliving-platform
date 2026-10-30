package com.coliving.platform.security.controller;

import com.coliving.platform.security.dto.AccessTokenResponse;
import com.coliving.platform.security.dto.SessionResponseDTO;
import com.coliving.platform.security.http.SessionCookieFactory;
import com.coliving.platform.security.service.AuthService;
import com.coliving.platform.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionCookieFactory sessionCookieFactory;
    private final UserRepository userRepository;

    @GetMapping("/user/session")
    public ResponseEntity<SessionResponseDTO> session(HttpServletRequest request) {
        return sessionCookieFactory.read(request)
                .flatMap(authService::sessionSubject)
                .flatMap(this::sessionResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private Optional<SessionResponseDTO> sessionResponse(String userId) {
        long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        return userRepository.findById(id)
                .map(user -> new SessionResponseDTO(true, userId, blankToNull(user.getNickname())));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        sessionCookieFactory.read(request).ifPresent(authService::closeSession);
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookieFactory.clear().toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/session/access")
    public ResponseEntity<AccessTokenResponse> accessToken(HttpServletRequest request) {
        return sessionCookieFactory.read(request)
                .flatMap(authService::currentAccessToken)
                .map(token -> ResponseEntity.ok(new AccessTokenResponse(token)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
