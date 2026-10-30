package com.coliving.platform.chat.service;

import com.coliving.platform.chat.document.ChatMessageDocument;
import com.coliving.platform.chat.dto.ChatMessageDTO;
import com.coliving.platform.chat.dto.ChatMessageListResponseDTO;
import com.coliving.platform.chat.dto.ChatRoomItemDTO;
import com.coliving.platform.chat.dto.ChatRoomOpenRequestDTO;
import com.coliving.platform.chat.dto.ChatRoomOpenResponseDTO;
import com.coliving.platform.chat.entity.ChatRoomEntity;
import com.coliving.platform.chat.entity.ChatRoomMemberEntity;
import com.coliving.platform.chat.entity.ChatRoomMemberId;
import com.coliving.platform.chat.entity.ChatSourceType;
import com.coliving.platform.chat.repository.ChatMessageRepository;
import com.coliving.platform.chat.repository.ChatRoomMemberRepository;
import com.coliving.platform.chat.repository.ChatRoomRepository;
import com.coliving.platform.client.ContractClient;
import com.coliving.platform.client.ContractClient.ResidentRoom;
import com.coliving.platform.client.UserClient;
import com.coliving.platform.client.UserClient.UserSummary;
import com.coliving.platform.common.auth.ResidentGuard;
import com.coliving.platform.common.auth.ResidentGuard.Resident;
import com.coliving.platform.common.exception.CommunityException;
import com.coliving.platform.common.exception.ErrorCode;
import com.coliving.platform.post.entity.PostEntity;
import com.coliving.platform.post.service.PostService;
import com.coliving.platform.safety.service.BlockQueryService;
import com.coliving.platform.safety.service.SanctionGuard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 입주자 1:1 채팅 (API 4장)
 * 채팅방·참여자·읽음 위치 = PostgreSQL / 메시지 = MongoDB chat_messages
 */
@Service
@RequiredArgsConstructor
public class ChatService {

	private static final Set<String> ACTIVE_RESIDENT_STATUSES = Set.of("RESIDING", "MOVE_OUT_SCHEDULED");
	private static final Pattern OBJECT_ID = Pattern.compile("^[0-9a-f]{24}$");
	private static final int MAX_PAGE_SIZE = 100;

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatUnreadService chatUnreadService;
	private final ResidentGuard residentGuard;
	private final BlockQueryService blockQueryService;
	private final SanctionGuard sanctionGuard;
	private final UserClient userClient;
	private final ContractClient contractClient;
	private final PostService postService;

	/** 4-1 채팅방 목록 — 퇴거자도 기존 방은 볼 수 있음, 내가 차단한 상대의 방은 숨김, 숨긴 메시지·삭제 메시지는 미리보기 없음 */
	@Transactional(readOnly = true)
	public List<ChatRoomItemDTO> getRooms(Long userId) {
		List<ChatRoomMemberEntity> mine = chatRoomMemberRepository.findByUserId(userId);
		if (mine.isEmpty()) {
			return List.of();
		}
		List<Long> roomIds = mine.stream().map(ChatRoomMemberEntity::getChatRoomId).toList();
		Map<Long, ChatRoomEntity> rooms = chatRoomRepository.findAllById(roomIds).stream()
				.collect(Collectors.toMap(ChatRoomEntity::getId, Function.identity()));
		Map<Long, Long> partnerByRoom = chatRoomMemberRepository.findByChatRoomIdIn(roomIds).stream()
				.filter(m -> !m.getUserId().equals(userId))
				.collect(Collectors.toMap(ChatRoomMemberEntity::getChatRoomId, ChatRoomMemberEntity::getUserId,
						(a, b) -> a));

		Set<Long> blocked = blockQueryService.getBlockedUserIds(userId);
		List<Long> userIds = new ArrayList<>(partnerByRoom.values());
		userIds.add(userId);
		Map<Long, UserSummary> users = userClient.getUsers(userIds);
		Map<Long, ResidentRoom> residentRooms = contractClient.getResidentRooms(partnerByRoom.values());
		boolean meActive = isActiveResident(users.get(userId));

		List<ChatRoomItemDTO> result = new ArrayList<>();
		for (ChatRoomMemberEntity member : mine) {
			ChatRoomEntity room = rooms.get(member.getChatRoomId());
			Long partnerId = partnerByRoom.get(member.getChatRoomId());
			if (room == null || partnerId == null || blocked.contains(partnerId)) {
				continue;
			}
			UserSummary partner = users.get(partnerId);
			ResidentRoom partnerRoom = residentRooms.get(partnerId);
			boolean readOnly = room.isReadOnly() || !meActive || !isActiveResident(partner);
			// 미리보기는 보는 사람 기준 — 차단 중 받은(나에게 숨긴) 메시지는 차단을 풀어도 안 보이게
			Optional<ChatMessageDocument> last = chatMessageRepository
					.findFirstByRoomIdAndHiddenForNotOrderByIdDesc(room.getId(), userId);
			String lastMessage = last.filter(ChatMessageDocument::isActive)
					.map(m -> ChatRoomEntity.preview(m.getContent()))
					.orElse(null);
			LocalDateTime lastMessageAt = last.map(ChatMessageDocument::getSentAt).orElse(null);
			result.add(new ChatRoomItemDTO(
					room.getId(),
					new ChatRoomItemDTO.Partner(partnerId,
							partner != null ? partner.name() : "알 수 없음",
							partner != null ? partner.profileImageUrl() : null,
							partnerRoom != null ? partnerRoom.roomNumber() : null),
					lastMessage,
					lastMessageAt,
					chatUnreadService.countUnread(room.getId(), userId, member.getLastReadMessageId()),
					readOnly));
		}
		Comparator<ChatRoomItemDTO> byLastMessage = Comparator.comparing(ChatRoomItemDTO::lastMessageAt,
				Comparator.nullsLast(Comparator.reverseOrder()));
		Comparator<ChatRoomItemDTO> byRoomId = Comparator.comparing(ChatRoomItemDTO::roomId, Comparator.reverseOrder());
		result.sort(byLastMessage.thenComparing(byRoomId));
		return result;
	}

	/** 4-2 채팅방 열기 — 같은 상대와의 방이 있으면 그 방, 없으면 새로 만듦 */
	@Transactional
	public ChatRoomOpenResponseDTO openRoom(Long userId, ChatRoomOpenRequestDTO request) {
		Resident me = residentGuard.requireResident(userId);
		sanctionGuard.checkCanWrite(userId);

		Long partnerId = request.partnerUserId();
		if (partnerId.equals(userId)) {
			throw new CommunityException(ErrorCode.CHAT_NOT_AVAILABLE, "본인과는 채팅할 수 없습니다.");
		}
		if (request.sourceType() == ChatSourceType.POST && request.sourcePostId() == null) {
			throw new CommunityException(ErrorCode.INVALID_REQUEST, "게시글에서 시작한 채팅은 sourcePostId가 필요합니다.");
		}
		if (request.sourceType() == ChatSourceType.POST) {
			PostEntity post = postService.findActivePost(request.sourcePostId());
			residentGuard.requireSameBuilding(me, post.getBuildingId());
		}
		UserSummary partner = userClient.getUser(partnerId);
		if (partner == null) {
			throw new CommunityException(ErrorCode.USER_NOT_FOUND);
		}
		ResidentRoom partnerRoom = contractClient.getResidentRooms(List.of(partnerId)).get(partnerId);
		if (!isActiveResident(partner) || partnerRoom == null) {
			throw new CommunityException(ErrorCode.CHAT_NOT_AVAILABLE);
		}
		residentGuard.requireSameBuilding(me, partnerRoom.buildingId());
		// 상대가 나를 차단했어도 정상으로 열림 (차단 사실을 알리지 않음). 내가 차단한 상대만 막음
		checkNotBlockedByMe(userId, partnerId);

		return chatRoomRepository.findByPairKey(ChatRoomEntity.pairKey(userId, partnerId))
				.map(room -> new ChatRoomOpenResponseDTO(room.getId(), false))
				.orElseGet(() -> {
					ChatRoomEntity room = chatRoomRepository.save(ChatRoomEntity.create(
							me.buildingId(), userId, partnerId, request.sourceType(), request.sourcePostId()));
					chatRoomMemberRepository.save(ChatRoomMemberEntity.create(room.getId(), userId));
					chatRoomMemberRepository.save(ChatRoomMemberEntity.create(room.getId(), partnerId));
					return new ChatRoomOpenResponseDTO(room.getId(), true);
				});
	}

	/** 4-3 메시지 목록 — 최신 size개 (before 있으면 그 이전), 응답은 오래된 것 → 최신 순, 나에게 숨긴 메시지 제외 */
	@Transactional(readOnly = true)
	public ChatMessageListResponseDTO getMessages(Long userId, Long roomId, String before, int size) {
		requireMember(roomId, userId);
		int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
		PageRequest page = PageRequest.of(0, pageSize + 1);   // 1개 더 가져와서 이전 메시지가 더 있는지 확인
		List<ChatMessageDocument> found;
		if (before == null || before.isBlank()) {
			found = chatMessageRepository.findByRoomIdAndHiddenForNotOrderByIdDesc(roomId, userId, page);
		} else {
			requireObjectId(before);
			found = chatMessageRepository.findByRoomIdAndIdLessThanAndHiddenForNotOrderByIdDesc(roomId, before, userId, page);
		}
		boolean hasMore = found.size() > pageSize;
		List<ChatMessageDTO> messages = new ArrayList<>(found.stream()
				.limit(pageSize)
				.map(ChatMessageDTO::from)
				.toList());
		Collections.reverse(messages);
		return new ChatMessageListResponseDTO(messages, hasMore);
	}

	/** 4-4 읽음 처리 — 이 방의 메시지 ID만 받음 */
	@Transactional
	public void markRead(Long userId, Long roomId, String lastReadMessageId) {
		ChatRoomMemberEntity member = requireMember(roomId, userId);
		requireObjectId(lastReadMessageId);
		chatMessageRepository.findById(lastReadMessageId)
				.filter(message -> message.getRoomId().equals(roomId) && message.isVisibleTo(userId))
				.orElseThrow(() -> new CommunityException(ErrorCode.MESSAGE_NOT_FOUND));
		member.readUpTo(lastReadMessageId);
	}

	/**
	 * 메시지 보내기 — 4-6 REST, 4-5 WebSocket(COL-33 ②)이 같이 씀
	 * 읽기 전용 → CHAT_READ_ONLY, 제재 중 → SANCTIONED, 내가 차단한 상대 → CHAT_NOT_AVAILABLE
	 * 상대가 나를 차단했으면 → 정상 응답(201)하지만 상대에게는 숨김 (차단 사실을 알리지 않음, 카카오톡 방식)
	 */
	@Transactional
	public ChatMessageDTO sendMessage(Long userId, Long roomId, String content) {
		ChatRoomMemberEntity member = requireMember(roomId, userId);
		ChatRoomEntity room = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new CommunityException(ErrorCode.ROOM_NOT_FOUND));
		Long partnerId = findPartnerId(roomId, userId);

		Map<Long, UserSummary> users = userClient.getUsers(List.of(userId, partnerId));
		if (room.isReadOnly() || !isActiveResident(users.get(userId)) || !isActiveResident(users.get(partnerId))) {
			throw new CommunityException(ErrorCode.CHAT_READ_ONLY);
		}
		sanctionGuard.checkCanWrite(userId);
		checkNotBlockedByMe(userId, partnerId);
		boolean blockedByPartner = blockQueryService.isBlockedBy(userId, partnerId);

		String trimmed = content.strip();
		ChatMessageDocument message = chatMessageRepository.save(
				ChatMessageDocument.create(roomId, userId, trimmed, blockedByPartner ? partnerId : null));
		if (!blockedByPartner) {
			room.updateLastMessage(trimmed, message.getSentAt());
		}
		member.readUpTo(message.getId());   // 내가 보낸 메시지까지는 읽은 것으로
		// TODO(COL-33 ②): 실시간 전송 — 숨김 메시지는 보낸 사람에게만, 상대 알림도 생략
		return ChatMessageDTO.from(message);
	}

	// ---------- 내부 ----------

	private ChatRoomMemberEntity requireMember(Long roomId, Long userId) {
		// 참여하지 않은 방은 있는지조차 알려주지 않음
		return chatRoomMemberRepository.findById(new ChatRoomMemberId(roomId, userId))
				.orElseThrow(() -> new CommunityException(ErrorCode.ROOM_NOT_FOUND));
	}

	private Long findPartnerId(Long roomId, Long userId) {
		return chatRoomMemberRepository.findByChatRoomId(roomId).stream()
				.map(ChatRoomMemberEntity::getUserId)
				.filter(id -> !id.equals(userId))
				.findFirst()
				.orElseThrow(() -> new CommunityException(ErrorCode.ROOM_NOT_FOUND));
	}

	/** 내가 차단한 상대와는 채팅 불가 (내가 한 차단이라 숨길 이유 없음) */
	private void checkNotBlockedByMe(Long userId, Long partnerId) {
		if (blockQueryService.isBlockedBy(partnerId, userId)) {
			throw new CommunityException(ErrorCode.CHAT_NOT_AVAILABLE);
		}
	}

	private void requireObjectId(String messageId) {
		if (messageId == null || !OBJECT_ID.matcher(messageId).matches()) {
			throw new CommunityException(ErrorCode.MESSAGE_NOT_FOUND);
		}
	}

	private boolean isActiveResident(UserSummary user) {
		return user != null && user.residentStatus() != null && ACTIVE_RESIDENT_STATUSES.contains(user.residentStatus());
	}
}
