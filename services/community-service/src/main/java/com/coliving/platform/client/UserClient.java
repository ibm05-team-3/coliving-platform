package com.coliving.platform.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * user-service 호출 (연동 요청 API 1-1, 1-2)
 * 지금은 FakeUserClient가 가짜 데이터를 돌려줌 → gRPC 서버가 나오면 GrpcUserClient로 교체
 */
public interface UserClient {

	/** 회원 일괄 조회 — 없는 ID는 결과에서 빠짐 */
	Map<Long, UserSummary> getUsers(Collection<Long> userIds);

	/** 관리자 쪽지 쓰기 — 이름 검색 */
	List<UserSummary> searchUsers(String keyword);

	default UserSummary getUser(Long userId) {
		return getUsers(List.of(userId)).get(userId);
	}

	/**
	 * @param role           MEMBER / CANDIDATE / RESIDENT / ADMIN
	 * @param residentStatus MOVE_IN_SCHEDULED / RESIDING / MOVE_OUT_SCHEDULED / MOVED_OUT (입주자가 아니면 null)
	 */
	record UserSummary(Long userId, String name, String profileImageUrl, String role, String residentStatus) {
	}
}
