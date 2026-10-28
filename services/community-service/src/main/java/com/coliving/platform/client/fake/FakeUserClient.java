package com.coliving.platform.client.fake;

import com.coliving.platform.client.UserClient;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** TODO: user-service gRPC 연동 후 GrpcUserClient로 교체하고 이 클래스 삭제 */
@Component
public class FakeUserClient implements UserClient {

	private static final Map<Long, UserSummary> USERS = List.of(
			new UserSummary(5001L, "김민서", null, "RESIDENT", "RESIDING"),
			new UserSummary(5002L, "박준영", null, "RESIDENT", "RESIDING"),
			new UserSummary(5003L, "이도윤", null, "RESIDENT", "RESIDING"),
			new UserSummary(6001L, "김예린", null, "CANDIDATE", null),
			new UserSummary(7001L, "정하늘", null, "MEMBER", null),
			new UserSummary(9001L, "망원 스테이 매니저", null, "ADMIN", null)
	).stream().collect(Collectors.toMap(UserSummary::userId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

	@Override
	public Map<Long, UserSummary> getUsers(Collection<Long> userIds) {
		return userIds.stream()
				.distinct()
				.filter(USERS::containsKey)
				.collect(Collectors.toMap(Function.identity(), USERS::get));
	}

	@Override
	public List<UserSummary> searchUsers(String keyword) {
		return USERS.values().stream()
				.filter(user -> !"ADMIN".equals(user.role()))
				.filter(user -> keyword == null || user.name().contains(keyword))
				.toList();
	}
}
