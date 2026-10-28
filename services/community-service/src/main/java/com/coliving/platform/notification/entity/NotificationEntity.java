package com.coliving.platform.notification.entity;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.common.time.KstTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long receiverId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "sender_type")
	private SenderType receiverType;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "notification_type")
	private NotificationType type;

	@Column(nullable = false, length = 50)
	private String title;

	@Column(nullable = false, length = 200)
	private String message;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "notification_target")
	private NotificationTarget targetType;

	@Column(nullable = false, length = 50)
	private String targetId;

	@Column(nullable = false)
	private boolean isRead;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	public static NotificationEntity create(Long receiverId, SenderType receiverType, NotificationType type,
			String title, String message, NotificationTarget targetType, String targetId) {
		NotificationEntity entity = new NotificationEntity();
		entity.receiverId = receiverId;
		entity.receiverType = receiverType;
		entity.type = type;
		entity.title = cut(title, 50);
		entity.message = cut(message, 200);
		entity.targetType = targetType;
		entity.targetId = targetId;
		return entity;
	}

	public void markRead() {
		this.isRead = true;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = KstTime.now();
		}
	}

	private static String cut(String value, int max) {
		return value.length() <= max ? value : value.substring(0, max - 1) + "…";
	}
}
