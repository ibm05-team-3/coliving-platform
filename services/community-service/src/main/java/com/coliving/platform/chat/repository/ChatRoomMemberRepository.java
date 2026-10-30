package com.coliving.platform.chat.repository;

import com.coliving.platform.chat.entity.ChatRoomMemberEntity;
import com.coliving.platform.chat.entity.ChatRoomMemberId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, ChatRoomMemberId> {

	List<ChatRoomMemberEntity> findByUserId(Long userId);

	List<ChatRoomMemberEntity> findByChatRoomIdIn(Collection<Long> chatRoomIds);

	List<ChatRoomMemberEntity> findByChatRoomId(Long chatRoomId);
}
