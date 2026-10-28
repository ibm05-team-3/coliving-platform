package com.coliving.platform.note.repository;

import com.coliving.platform.note.entity.NoteThreadEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteThreadRepository extends JpaRepository<NoteThreadEntity, Long>,
		JpaSpecificationExecutor<NoteThreadEntity> {

	Optional<NoteThreadEntity> findByUserId(Long userId);
}
