package com.coliving.platform.post.repository;

import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.post.entity.PostCategory;
import com.coliving.platform.post.entity.PostEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {

	/** 상단 고정 공지 */
	List<PostEntity> findByBuildingIdAndCategoryAndStatusAndPinnedTrueOrderByCreatedAtDesc(
			Long buildingId, PostCategory category, ContentStatus status);

	/** 이번 주 인기글 상위 3개 (공지 제외) */
	List<PostEntity> findTop3ByBuildingIdAndStatusAndCategoryNotAndCreatedAtAfterOrderByViewCountDescIdDesc(
			Long buildingId, ContentStatus status, PostCategory excluded, LocalDateTime after);

	long countByAuthorIdAndBuildingIdAndStatus(Long authorId, Long buildingId, ContentStatus status);

	@Modifying
	@Query("update PostEntity p set p.viewCount = p.viewCount + 1 where p.id = :postId")
	int increaseViewCount(@Param("postId") Long postId);

	@Modifying
	@Query("update PostEntity p set p.commentCount = p.commentCount + :delta where p.id = :postId")
	int addCommentCount(@Param("postId") Long postId, @Param("delta") int delta);
}
