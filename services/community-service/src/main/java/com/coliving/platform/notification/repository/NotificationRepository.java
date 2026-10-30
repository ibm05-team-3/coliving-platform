package com.coliving.platform.notification.repository;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.notification.entity.NotificationEntity;
import com.coliving.platform.notification.entity.NotificationType;
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

	/** 이 방의 안 읽은 채팅 알림 지우기 (최신 메시지 알림으로 바꾸기 전) */
	default int deleteUnreadChat(Long userId, String roomId) {
		return deleteUnread(SenderType.USER, userId, NotificationType.CHAT, roomId);
	}

	/** 이 방의 채팅 알림 모두 읽음 */
	default int markChatRoomRead(Long userId, String roomId) {
		return markTargetRead(SenderType.USER, userId, NotificationType.CHAT, roomId);
	}

	// enum 값은 JPQL 에 글자로 쓰지 않고 파라미터로 넘김
	// (글자로 쓰면 Hibernate 가 'USER'::SenderType 처럼 자바 클래스 이름으로 형변환해서 DB 타입 sender_type 을 못 찾음)

	@Modifying
	@Query("delete from NotificationEntity n "
			+ "where n.receiverType = :receiverType and n.receiverId = :receiverId "
			+ "and n.type = :type and n.targetId = :targetId and n.isRead = false")
	int deleteUnread(@Param("receiverType") SenderType receiverType, @Param("receiverId") Long receiverId,
			@Param("type") NotificationType type, @Param("targetId") String targetId);

	@Modifying
	@Query("update NotificationEntity n set n.isRead = true "
			+ "where n.receiverType = :receiverType and n.receiverId = :receiverId "
			+ "and n.type = :type and n.targetId = :targetId and n.isRead = false")
	int markTargetRead(@Param("receiverType") SenderType receiverType, @Param("receiverId") Long receiverId,
			@Param("type") NotificationType type, @Param("targetId") String targetId);
}
