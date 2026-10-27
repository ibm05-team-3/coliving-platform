package com.coliving.platform.common.exception;

/** 매물 서비스가 꺼져 있거나 응답 시간(deadline)을 넘겼을 때 → CustomControllerAdvice에서 503으로 응답 */
public class RoomServiceUnavailableException extends RuntimeException {
    public RoomServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
