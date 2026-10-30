package com.coliving.platform.chat.controller;

import com.coliving.platform.chat.dto.ChatMessageDTO;
import com.coliving.platform.chat.dto.ChatMessageListResponseDTO;
import com.coliving.platform.chat.dto.ChatMessageSendRequestDTO;
import com.coliving.platform.chat.dto.ChatReadRequestDTO;
import com.coliving.platform.chat.dto.ChatRoomItemDTO;
import com.coliving.platform.chat.dto.ChatRoomOpenRequestDTO;
import com.coliving.platform.chat.dto.ChatRoomOpenResponseDTO;
import com.coliving.platform.chat.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 1:1 채팅 REST (API 4-1 ~ 4-4, 4-6, 4-7) — 실시간은 WebSocket(4-5) */
@RestController
@RequestMapping("/community-service/chat/rooms")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	/** 4-1 채팅방 목록 */
	@GetMapping
	public List<ChatRoomItemDTO> getRooms(@RequestHeader("X-User") Long userId) {
		return chatService.getRooms(userId);
	}

	/** 4-2 채팅방 열기 — 기존 방 200, 새 방 201 */
	@PostMapping
	public ResponseEntity<ChatRoomOpenResponseDTO> openRoom(@RequestHeader("X-User") Long userId,
			@Valid @RequestBody ChatRoomOpenRequestDTO request) {
		ChatRoomOpenResponseDTO response = chatService.openRoom(userId, request);
		return ResponseEntity.status(response.created() ? HttpStatus.CREATED : HttpStatus.OK).body(response);
	}

	/** 4-3 메시지 목록 */
	@GetMapping("/{roomId}/messages")
	public ChatMessageListResponseDTO getMessages(@RequestHeader("X-User") Long userId, @PathVariable Long roomId,
			@RequestParam(required = false) String before, @RequestParam(defaultValue = "30") int size) {
		return chatService.getMessages(userId, roomId, before, size);
	}

	/** 4-4 읽음 처리 */
	@PatchMapping("/{roomId}/read")
	public ResponseEntity<Void> markRead(@RequestHeader("X-User") Long userId, @PathVariable Long roomId,
			@Valid @RequestBody ChatReadRequestDTO request) {
		chatService.markRead(userId, roomId, request.lastReadMessageId());
		return ResponseEntity.noContent().build();
	}

	/** 4-7 채팅방 나가기 */
	@PostMapping("/{roomId}/leave")
	public ResponseEntity<Void> leaveRoom(@RequestHeader("X-User") Long userId, @PathVariable Long roomId) {
		chatService.leaveRoom(userId, roomId);
		return ResponseEntity.noContent().build();
	}

	/** 4-6 메시지 보내기 (REST) — WebSocket 연결이 안 될 때 대신 사용, 테스트용 */
	@PostMapping("/{roomId}/messages")
	public ResponseEntity<ChatMessageDTO> sendMessage(@RequestHeader("X-User") Long userId, @PathVariable Long roomId,
			@Valid @RequestBody ChatMessageSendRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(chatService.sendMessage(userId, roomId, request.content()));
	}
}
