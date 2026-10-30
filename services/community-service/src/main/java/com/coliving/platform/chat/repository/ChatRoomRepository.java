package com.coliving.platform.chat.repository;

import com.coliving.platform.chat.entity.ChatRoomEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

	Optional<ChatRoomEntity> findByPairKey(String pairKey);
}
