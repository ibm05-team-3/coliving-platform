package com.coliving.platform.note.repository;

import com.coliving.platform.note.entity.NoteEntity;
import com.coliving.platform.note.entity.NoteThreadEntity;
import com.coliving.platform.note.entity.NoteThreadStatus;
import com.coliving.platform.note.entity.NoteThreadType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** 관리자 쪽지함 목록 검색 조건 (null인 조건은 빼고 검색) */
public final class NoteThreadSpecs {

	private NoteThreadSpecs() {
	}

	/**
	 * @param matchedUserIds 이름 검색에 걸린 회원 ID (유저 서비스 검색 결과)
	 */
	public static Specification<NoteThreadEntity> search(NoteThreadType type, NoteThreadStatus status,
			String keyword, Collection<Long> matchedUserIds) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (type != null) {
				predicates.add(cb.equal(root.get("threadType"), type));
			}
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}
			if (keyword != null && !keyword.isBlank()) {
				String like = "%" + keyword.trim() + "%";
				List<Predicate> keywordMatches = new ArrayList<>();

				Join<Object, Object> guest = root.join("guestInquiry", JoinType.LEFT);
				keywordMatches.add(cb.like(guest.get("name"), like));

				Subquery<Long> noteMatch = query.subquery(Long.class);
				Root<NoteEntity> note = noteMatch.from(NoteEntity.class);
				noteMatch.select(note.get("id"))
						.where(cb.equal(note.get("noteThread"), root), cb.like(note.get("content"), like));
				keywordMatches.add(cb.exists(noteMatch));

				if (matchedUserIds != null && !matchedUserIds.isEmpty()) {
					keywordMatches.add(root.get("userId").in(matchedUserIds));
				}
				predicates.add(cb.or(keywordMatches.toArray(Predicate[]::new)));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}
}
