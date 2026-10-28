package com.coliving.platform.note.repository;

import com.coliving.platform.common.entity.SenderType;
import com.coliving.platform.note.entity.NoteEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {

	Page<NoteEntity> findByNoteThread_Id(Long threadId, Pageable pageable);

	List<NoteEntity> findByNoteThread_IdOrderByCreatedAtAsc(Long threadId);

	Optional<NoteEntity> findFirstByNoteThread_IdOrderByCreatedAtDesc(Long threadId);

	/** 안 읽은 쪽지 수 — senderType: 상대방이 보낸 쪽 */
	long countByNoteThread_IdAndSenderTypeAndReadAtIsNull(Long threadId, SenderType senderType);

	/** 상대방이 보낸 쪽지 전체 읽음 */
	@Modifying
	@Query("update NoteEntity n set n.readAt = :readAt "
			+ "where n.noteThread.id = :threadId and n.senderType = :senderType and n.readAt is null")
	int markAllRead(@Param("threadId") Long threadId, @Param("senderType") SenderType senderType,
			@Param("readAt") OffsetDateTime readAt);
}
