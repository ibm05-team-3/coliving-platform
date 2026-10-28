package com.coliving.platform.common.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 모든 오류를 ProblemDetail(application/problem+json)로 응답
 * - CommunityException: 부모 클래스가 ProblemDetail 그대로 응답
 * - @Valid 실패 / 잘못된 값: 400 INVALID_REQUEST
 * - X-User 헤더 없음: 401 UNAUTHORIZED
 * - 업로드 30MB 초과: 413 FILE_TOO_LARGE
 */
@RestControllerAdvice
public class CustomControllerAdvice extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse(ErrorCode.INVALID_REQUEST.getMessage());
		return problem(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, detail);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return problem(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		if (ex instanceof MissingRequestHeaderException missing && "X-User".equalsIgnoreCase(missing.getHeaderName())) {
			return problem(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
		}
		return problem(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, ex.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return problem(HttpStatus.CONTENT_TOO_LARGE, ErrorCode.FILE_TOO_LARGE, ErrorCode.FILE_TOO_LARGE.getMessage());
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return problem(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, ex.getName() + " 값이 올바르지 않습니다.");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
		return problem(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, ex.getMessage());
	}

	private ResponseEntity<Object> problem(HttpStatus status, ErrorCode code, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty("code", code.name());
		return ResponseEntity.status(status).body(problem);
	}
}
