package com.coliving.platform.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 화면 분기용 오류 코드 (API 명세서 0-3)
 * ProblemDetail의 "code" 속성으로 내려감
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 400
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
	REPLY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "답글에는 답글을 달 수 없습니다."),
	GUEST_REPLY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "비회원 문의는 쪽지로 답장할 수 없습니다."),
	INVALID_DURATION(HttpStatus.BAD_REQUEST, "이용 제한 기간은 1·3·7·30일 중 하나여야 합니다."),

	// 401
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

	// 403
	NOT_RESIDENT(HttpStatus.FORBIDDEN, "입주자만 이용할 수 있습니다."),
	NOT_OWNER(HttpStatus.FORBIDDEN, "본인이 작성한 글만 수정·삭제할 수 있습니다."),
	OTHER_BUILDING(HttpStatus.FORBIDDEN, "다른 건물의 정보는 볼 수 없습니다."),
	SANCTIONED(HttpStatus.FORBIDDEN, "커뮤니티 이용이 제한된 상태입니다."),
	CHAT_NOT_AVAILABLE(HttpStatus.FORBIDDEN, "지금은 이 사용자와 채팅할 수 없습니다."),
	CANNOT_REPORT_SELF(HttpStatus.FORBIDDEN, "본인은 신고할 수 없습니다."),
	CANNOT_BLOCK_SELF(HttpStatus.FORBIDDEN, "본인은 차단할 수 없습니다."),
	ADMIN_ONLY(HttpStatus.FORBIDDEN, "관리자만 이용할 수 있습니다."),

	// 404
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "쪽지함을 찾을 수 없습니다."),
	ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
	MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
	DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "문서를 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."),
	SANCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "제재 내역을 찾을 수 없습니다."),
	BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "차단 내역이 없습니다."),
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

	// 409
	REINDEX_IN_PROGRESS(HttpStatus.CONFLICT, "이미 반영 작업이 진행 중입니다."),
	ALREADY_REPORTED(HttpStatus.CONFLICT, "이미 신고한 내용입니다."),
	ALREADY_BLOCKED(HttpStatus.CONFLICT, "이미 차단한 사용자입니다."),
	ALREADY_HANDLED(HttpStatus.CONFLICT, "이미 처리된 신고입니다."),
	SANCTION_ALREADY_REVOKED(HttpStatus.CONFLICT, "이미 해제되었거나 만료된 제재입니다."),

	// 413 · 429
	FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "파일은 최대 30MB까지 올릴 수 있습니다."),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.");

	private final HttpStatus status;
	private final String message;
}
