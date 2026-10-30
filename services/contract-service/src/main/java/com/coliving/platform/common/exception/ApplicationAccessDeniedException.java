package com.coliving.platform.common.exception;

/**
 * 로그인한 회원(X-User)의 신청서가 아님 → 403
 * (Spring Security의 AccessDeniedException과 이름이 겹치지 않게 따로 만든다)
 */
public class ApplicationAccessDeniedException extends RuntimeException {
    public ApplicationAccessDeniedException(String message) {
        super(message);
    }
}
