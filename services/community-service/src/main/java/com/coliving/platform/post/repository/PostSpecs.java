package com.coliving.platform.post.repository;

import com.coliving.platform.common.entity.ContentStatus;
import com.coliving.platform.post.entity.PostCategory;
import com.coliving.platform.post.entity.PostEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** 게시글 목록 검색 조건 (5-1) */
public final class PostSpecs {

	private PostSpecs() {
	}

	/**
	 * 내 건물 · 정상 글 · 공지 제외 · (카테고리) · (제목·내용 검색) · 내가 차단한 사람 글 제외
	 */
	public static Specification<PostEntity> list(Long buildingId, PostCategory category, String keyword,
			Collection<Long> blockedUserIds) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get("buildingId"), buildingId));
			predicates.add(cb.equal(root.get("status"), ContentStatus.ACTIVE));
			predicates.add(cb.notEqual(root.get("category"), PostCategory.NOTICE));
			if (category != null) {
				predicates.add(cb.equal(root.get("category"), category));
			}
			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				predicates.add(cb.or(cb.like(root.get("title"), like), cb.like(root.get("content"), like)));
			}
			if (blockedUserIds != null && !blockedUserIds.isEmpty()) {
				predicates.add(cb.not(root.get("authorId").in(blockedUserIds)));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}
}
