package com.coliving.platform.chat.repository;

import com.coliving.platform.chat.entity.ChatRoomMemberEntity;
import com.coliving.platform.chat.entity.ChatRoomMemberId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, ChatRoomMemberId> {

	List<ChatRoomMemberEntity> findByUserId(Long userId);

	/** 내가 나가지 않은 방 (4-1 목록, 안 읽은 수) */
	List<ChatRoomMemberEntity> findByUserIdAndLeftAtIsNull(Long userId);

	List<ChatRoomMemberEntity> findByChatRoomIdIn(Collection<Long> chatRoomIds);

	List<ChatRoomMemberEntity> findByChatRoomId(Long chatRoomId);
}
