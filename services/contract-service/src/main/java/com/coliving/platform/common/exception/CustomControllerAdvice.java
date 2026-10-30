package com.coliving.platform.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MissingRequestHeaderException;
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
    // ---- 회원 서비스(gRPC) 호출 관련 ----

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
            UserNotFoundException e, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<Map<String, Object>> handleUserService(
            UserServiceException e, HttpServletRequest request) {
        e.printStackTrace();
        return buildError(HttpStatus.BAD_GATEWAY,
                "회원 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", request.getRequestURI());
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleUserServiceUnavailable(
            UserServiceUnavailableException e, HttpServletRequest request) {
        e.printStackTrace();
        return buildError(HttpStatus.SERVICE_UNAVAILABLE,
                "연결된 서비스가 응답하지 않습니다. 잠시 후 다시 시도해주세요.", request.getRequestURI());
    }

    // ---- 신청서(contract_application) 관련 ----

    // X-User 헤더 없음 = gateway를 거쳤는데 로그인하지 않은 요청 (gateway가 헤더를 지움)
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(
            MissingRequestHeaderException e, HttpServletRequest request) {
        if ("X-User".equals(e.getHeaderName())) {
            return buildError(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", request.getRequestURI());
        }
        return buildError(HttpStatus.BAD_REQUEST, "필수 헤더가 없습니다: " + e.getHeaderName(), request.getRequestURI());
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationNotFound(
            ApplicationNotFoundException e, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
    }

    // 남의 신청서 조회·제출 시도
    @ExceptionHandler(ApplicationAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationAccessDenied(
            ApplicationAccessDeniedException e, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, e.getMessage(), request.getRequestURI());
    }

    // 엔티티·서비스의 값 검증 실패 (계약 기간, 금액, 약관 미동의 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI());
    }

    // 지금 상태에서 할 수 없는 요청 (DRAFT가 아닌데 제출, 작성 기한 지남 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException e, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, e.getMessage(), request.getRequestURI());
    }

    // @Version 충돌: 같은 신청서를 두 곳에서 동시에 수정
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(
            ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT,
                "다른 곳에서 신청서가 먼저 변경됐습니다. 새로고침 후 다시 시도해주세요.", request.getRequestURI());
    }

    // DB 제약 위반. 23P01 = no_overlap_booking(같은 호실·겹치는 기간)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException e, HttpServletRequest request) {
        e.printStackTrace();
        if ("23P01".equals(sqlState(e))) {
            return buildError(HttpStatus.CONFLICT,
                    "선택한 기간에 이미 진행 중인 신청이 있어요. 다른 입주일을 선택해주세요.", request.getRequestURI());
        }
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "신청서를 저장하지 못했습니다.", request.getRequestURI());
    }

    // 예외 안쪽(cause)을 따라가며 PostgreSQL SQLState를 찾는다
    private String sqlState(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof java.sql.SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return null;
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