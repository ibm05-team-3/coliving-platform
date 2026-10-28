package com.coliving.platform.note.entity;

/** 쪽지함 유형 (DB enum note_thread_type) */
public enum NoteThreadType {
	CANDIDATE,
	RESIDENT,
	MEMBER,
	GUEST;

	/** 유저 서비스 role → 쪽지함 유형 */
	public static NoteThreadType fromRole(String role) {
		if ("RESIDENT".equals(role)) {
			return RESIDENT;
		}
		if ("CANDIDATE".equals(role)) {
			return CANDIDATE;
		}
		return MEMBER;
	}
}
