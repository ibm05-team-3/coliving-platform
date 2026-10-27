package com.coliving.platform.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class CustomControllerAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("접근 권한이 없습니다.");
    }

    // ---- 매물 서비스(gRPC) 호출 관련 ----
    // gRPC 에러(StatusRuntimeException)는 RoomQueryService에서 아래 3개 예외로 바꿔서 던진다.
    // 아래 핸들러들은 RuntimeException 핸들러보다 구체적인 타입이라 먼저 선택된다.

    // 호실이 없음(매물 NOT_FOUND) 또는 삭제된 호실
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoomNotFound(
            RoomNotFoundException e, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
    }

    // 매물 서비스가 INTERNAL 등 에러로 응답한 경우
    @ExceptionHandler(RoomServiceException.class)
    public ResponseEntity<Map<String, Object>> handleRoomService(
            RoomServiceException e, HttpServletRequest request) {
        e.printStackTrace();
        return buildError(HttpStatus.BAD_GATEWAY,
                "호실 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", request.getRequestURI());
    }

    // 매물 서비스가 꺼져 있거나(UNAVAILABLE) 응답 시간을 넘긴 경우(DEADLINE_EXCEEDED)
    @ExceptionHandler(RoomServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleRoomServiceUnavailable(
            RoomServiceUnavailableException e, HttpServletRequest request) {
        e.printStackTrace();
        return buildError(HttpStatus.SERVICE_UNAVAILABLE,
                "연결된 서비스가 응답하지 않습니다. 잠시 후 다시 시도해주세요.", request.getRequestURI());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("잘못된 요청입니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception e, HttpServletRequest request) {

        e.printStackTrace();
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, String path) {

        Map<String, Object> body = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", path,
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.status(status).body(body);
    }
}