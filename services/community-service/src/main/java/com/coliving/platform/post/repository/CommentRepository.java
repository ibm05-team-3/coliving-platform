package com.coliving.platform.post.repository;

import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.post.entity.CommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

	List<CommentEntity> findByPost_IdOrderByCreatedAtAscIdAsc(Long postId);

	/** 내 글에 다른 사람이 단 정상 댓글 수 (5-9 받은 댓글) */
	@Query("select count(c) from CommentEntity c "
			+ "where c.post.authorId = :userId and c.post.status = :status "
			+ "and c.status = :status and c.authorId <> :userId")
	long countReceivedComments(@Param("userId") Long userId, @Param("status") ContentStatus status);
}
