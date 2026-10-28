package com.coliving.platform.common.exception;

import java.util.Map;
import lombok.Getter;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * 팀 규칙: ErrorResponseException + ProblemDetail (RFC 9457)
 * 사용 예) throw new CommunityException(ErrorCode.POST_NOT_FOUND);
 *        throw new CommunityException(ErrorCode.SANCTIONED, "커뮤니티 이용이 제한된 상태입니다. (11-03 10:00 해제)",
 *                                     Map.of("sanctionType", "RESTRICTION", "endsAt", endsAt));
 */
@Getter
public class CommunityException extends ErrorResponseException {

	private final ErrorCode errorCode;

	public CommunityException(ErrorCode errorCode) {
		this(errorCode, errorCode.getMessage(), Map.of());
	}

	public CommunityException(ErrorCode errorCode, String detail) {
		this(errorCode, detail, Map.of());
	}

	public CommunityException(ErrorCode errorCode, String detail, Map<String, Object> properties) {
		super(errorCode.getStatus(), createProblem(errorCode, detail, properties), null);
		this.errorCode = errorCode;
	}

	private static ProblemDetail createProblem(ErrorCode errorCode, String detail, Map<String, Object> properties) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
		problem.setProperty("code", errorCode.name());
		properties.forEach(problem::setProperty);
		return problem;
	}
}
