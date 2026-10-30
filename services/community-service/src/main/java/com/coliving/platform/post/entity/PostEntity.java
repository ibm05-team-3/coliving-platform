package com.coliving.platform.post.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.common.entity.SenderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 게시글 — 입주자 전용, 건물별 분리, 소프트 삭제 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long buildingId;

	@Column(nullable = false)
	private Long authorId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "sender_type")
	private SenderType authorType;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "post_category")
	private PostCategory category;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private int viewCount;

	@Column(nullable = false)
	private int commentCount;

	@Column(nullable = false)
	private boolean pinned;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "content_status")
	private ContentStatus status;

	private LocalDateTime deletedAt;

	public static PostEntity create(Long buildingId, Long authorId, SenderType authorType, PostCategory category,
			String title, String content) {
		PostEntity post = new PostEntity();
		post.buildingId = buildingId;
		post.authorId = authorId;
		post.authorType = authorType;
		post.category = category;
		post.title = title;
		post.content = content;
		post.pinned = category == PostCategory.NOTICE;   // 공지는 상단 고정
		post.status = ContentStatus.ACTIVE;
		return post;
	}

	public void update(PostCategory category, String title, String content) {
		this.category = category;
		this.title = title;
		this.content = content;
	}

	public void delete() {
		this.status = ContentStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
	}

	/** 관리자 숨김 (신고 처리) */
	public void hide() {
		this.status = ContentStatus.HIDDEN;
	}

	public boolean isActive() {
		return status == ContentStatus.ACTIVE;
	}

	public boolean isWrittenBy(Long userId) {
		return authorId.equals(userId);
	}
}
