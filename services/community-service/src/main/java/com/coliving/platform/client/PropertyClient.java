package com.coliving.platform.client;

import java.util.List;

/** property-service 호출 (연동 요청 API 3-1) */
public interface PropertyClient {

	List<BuildingSummary> getBuildings();

	default String getBuildingName(Long buildingId) {
		if (buildingId == null) {
			return null;
		}
		return getBuildings().stream()
				.filter(building -> building.buildingId().equals(buildingId))
				.map(BuildingSummary::name)
				.findFirst()
				.orElse(null);
	}

	record BuildingSummary(Long buildingId, String name) {
	}
}
