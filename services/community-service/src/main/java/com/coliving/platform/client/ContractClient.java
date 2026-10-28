package com.coliving.platform.client;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

/** contract-service 호출 (연동 요청 API 2-1) */
public interface ContractClient {

	/** 입주 건물·호실 일괄 조회 — 계약 없는 회원은 결과에서 빠짐 */
	Map<Long, ResidentRoom> getResidentRooms(Collection<Long> userIds);

	record ResidentRoom(Long userId, Long contractId, Long buildingId, String buildingName, String roomNumber,
			String contractStatus, LocalDate moveInDate, LocalDate contractEndDate) {
	}
}
