package com.coliving.platform.common.exception;

/** 매물 서비스가 NOT_FOUND·UNAVAILABLE 외의 gRPC 에러(INTERNAL 등)를 돌려줬을 때 → CustomControllerAdvice에서 502로 응답 */
public class RoomServiceException extends RuntimeException {
    public RoomServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
