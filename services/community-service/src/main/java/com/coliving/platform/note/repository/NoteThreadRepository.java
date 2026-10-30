package com.coliving.platform.note.repository;

import com.coliving.platform.note.entity.NoteThreadEntity;
import com.coliving.platform.note.entity.NoteThreadStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteThreadRepository extends JpaRepository<NoteThreadEntity, Long>,
		JpaSpecificationExecutor<NoteThreadEntity> {

	Optional<NoteThreadEntity> findByUserId(Long userId);

	/** 관리자 사이드바 쪽지함 배지 (미답변 쪽지함 수) */
	long countByStatus(NoteThreadStatus status);
}
