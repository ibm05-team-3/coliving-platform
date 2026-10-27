package com.coliving.platform.common.exception;

/** 호실이 없거나(매물 서비스 NOT_FOUND) 삭제된 호실일 때 → CustomControllerAdvice에서 404로 응답 */
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
