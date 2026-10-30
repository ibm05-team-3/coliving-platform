package com.coliving.platform.safety.repository;

import com.coliving.platform.safety.entity.SanctionType;
import com.coliving.platform.safety.entity.UserSanctionEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSanctionRepository extends JpaRepository<UserSanctionEntity, Long>,
		JpaSpecificationExecutor<UserSanctionEntity> {

	/** 진행 중인 이용 제한·정지 (가장 최근 것부터) */
	@Query("select s from UserSanctionEntity s where s.userId = :userId and s.sanctionType in :types "
			+ "and s.revokedAt is null and (s.endsAt is null or s.endsAt > :now) "
			+ "order by s.endsAt desc nulls first, s.id desc")
	List<UserSanctionEntity> findActive(@Param("userId") Long userId,
			@Param("types") Collection<SanctionType> types, @Param("now") LocalDateTime now);

	List<UserSanctionEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

	long countByUserIdAndSanctionType(Long userId, SanctionType sanctionType);
}
