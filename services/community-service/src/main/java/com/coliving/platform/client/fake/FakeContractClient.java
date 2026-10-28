package com.coliving.platform.client.fake;

import com.coliving.platform.client.ContractClient;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** TODO: contract-service gRPC 연동 후 GrpcContractClient로 교체하고 이 클래스 삭제 */
@Component
public class FakeContractClient implements ContractClient {

	private static final Map<Long, ResidentRoom> ROOMS = List.of(
			new ResidentRoom(5001L, 9101L, 1L, "망원 스테이", "302호", "ACTIVE", LocalDate.of(2025, 11, 3), LocalDate.of(2026, 11, 2)),
			new ResidentRoom(5002L, 9102L, 1L, "망원 스테이", "201호", "ACTIVE", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 8, 31)),
			new ResidentRoom(5003L, 9103L, 1L, "망원 스테이", "405호", "ACTIVE", LocalDate.of(2025, 10, 1), LocalDate.of(2026, 9, 30)),
			new ResidentRoom(6001L, 9201L, 1L, "망원 스테이", "302호", "PAYMENT_PENDING", LocalDate.of(2025, 11, 3), LocalDate.of(2026, 11, 2))
	).stream().collect(Collectors.toMap(ResidentRoom::userId, Function.identity()));

	@Override
	public Map<Long, ResidentRoom> getResidentRooms(Collection<Long> userIds) {
		return userIds.stream()
				.distinct()
				.filter(ROOMS::containsKey)
				.collect(Collectors.toMap(Function.identity(), ROOMS::get));
	}
}
