package com.coliving.platform.post.entity;

import com.coliving.platform.common.entity.BaseTimeEntity;
import com.coliving.platform.common.entity.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 댓글 · 대댓글 (대댓글은 1단계까지) */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id")
	private PostEntity post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_comment_id")
	private CommentEntity parentComment;

	@Column(nullable = false)
	private Long authorId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(nullable = false, columnDefinition = "content_status")
	private ContentStatus status;

	private LocalDateTime deletedAt;

	public static CommentEntity create(PostEntity post, CommentEntity parentComment, Long authorId, String content) {
		CommentEntity comment = new CommentEntity();
		comment.post = post;
		comment.parentComment = parentComment;
		comment.authorId = authorId;
		comment.content = content;
		comment.status = ContentStatus.ACTIVE;
		return comment;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void delete() {
		this.status = ContentStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isActive() {
		return status == ContentStatus.ACTIVE;
	}

	public boolean isReply() {
		return parentComment != null;
	}

	public boolean isWrittenBy(Long userId) {
		return authorId.equals(userId);
	}
}
