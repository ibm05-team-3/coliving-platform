package com.coliving.platform.notification.repository;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.notification.entity.NotificationEntity;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

	/** 받는 사람 알림 목록 (unreadOnly = true 면 안 읽은 것만) */
	@Query("select n from NotificationEntity n "
			+ "where n.receiverType = :receiverType and n.receiverId in :receiverIds "
			+ "and (:unreadOnly = false or n.isRead = false) "
			+ "order by n.createdAt desc, n.id desc")
	Page<NotificationEntity> findForReceiver(@Param("receiverType") SenderType receiverType,
			@Param("receiverIds") Collection<Long> receiverIds, @Param("unreadOnly") boolean unreadOnly,
			Pageable pageable);

	@Query("select count(n) from NotificationEntity n "
			+ "where n.receiverType = :receiverType and n.receiverId in :receiverIds and n.isRead = false")
	long countUnread(@Param("receiverType") SenderType receiverType,
			@Param("receiverIds") Collection<Long> receiverIds);

	@Modifying
	@Query("update NotificationEntity n set n.isRead = true "
			+ "where n.receiverType = :receiverType and n.receiverId in :receiverIds and n.isRead = false")
	int markAllRead(@Param("receiverType") SenderType receiverType,
			@Param("receiverIds") Collection<Long> receiverIds);
}
