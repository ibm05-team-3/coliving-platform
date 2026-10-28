package com.coliving.platform.client.fake;

import com.coliving.platform.client.PropertyClient;
import java.util.List;
import org.springframework.stereotype.Component;

/** TODO: property-service gRPC 연동 후 GrpcPropertyClient로 교체하고 이 클래스 삭제 */
@Component
public class FakePropertyClient implements PropertyClient {

	@Override
	public List<BuildingSummary> getBuildings() {
		return List.of(
				new BuildingSummary(1L, "망원 스테이"),
				new BuildingSummary(2L, "성수 하우스"),
				new BuildingSummary(3L, "연남 리빙")
		);
	}
}
