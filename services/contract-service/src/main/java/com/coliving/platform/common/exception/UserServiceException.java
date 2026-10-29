package com.coliving.platform.common.exception;

public class UserServiceException extends RuntimeException {
    public UserServiceException(String message, Throwable cause) {
        super(message,cause);
    }
}
