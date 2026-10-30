package com.coliving.platform.chat.service;

import com.coliving.platform.chat.entity.ChatRoomMemberEntity;
import com.coliving.platform.chat.repository.ChatMessageRepository;
import com.coliving.platform.chat.repository.ChatRoomMemberRepository;
import com.coliving.platform.safety.service.BlockQueryService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 안 읽은 채팅 수 (4-1 방별, 6-2 전체) — 알림 서비스와 채팅 서비스가 같이 씀 */
@Service
@RequiredArgsConstructor
public class ChatUnreadService {

	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final BlockQueryService blockQueryService;

	/** 한 방에서 상대가 보낸 메시지 중 안 읽은 수 (차단 중 받은, 나에게 숨긴 메시지 제외) */
	public long countUnread(Long roomId, Long userId, String lastReadMessageId) {
		return lastReadMessageId == null
				? chatMessageRepository.countByRoomIdAndSenderIdNotAndHiddenForNot(roomId, userId, userId)
				: chatMessageRepository.countByRoomIdAndSenderIdNotAndIdGreaterThanAndHiddenForNot(roomId, userId,
						lastReadMessageId, userId);
	}

	/** 내 모든 방의 안 읽은 수 합 (내가 차단한 상대 방 제외) */
	@Transactional(readOnly = true)
	public long countAllUnread(Long userId) {
		List<ChatRoomMemberEntity> mine = chatRoomMemberRepository.findByUserId(userId);
		if (mine.isEmpty()) {
			return 0;
		}
		Set<Long> blocked = blockQueryService.getBlockedUserIds(userId);
		List<ChatRoomMemberEntity> all = chatRoomMemberRepository.findByChatRoomIdIn(
				mine.stream().map(ChatRoomMemberEntity::getChatRoomId).toList());
		long total = 0;
		for (ChatRoomMemberEntity member : mine) {
			boolean partnerBlocked = all.stream()
					.anyMatch(m -> m.getChatRoomId().equals(member.getChatRoomId())
							&& !m.getUserId().equals(userId) && blocked.contains(m.getUserId()));
			if (!partnerBlocked) {
				total += countUnread(member.getChatRoomId(), userId, member.getLastReadMessageId());
			}
		}
		return total;
	}
}
