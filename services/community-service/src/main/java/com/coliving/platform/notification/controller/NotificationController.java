package com.coliving.platform.notification.controller;

import com.coliving.platform.common.dto.PageResponse;
import com.coliving.platform.notification.dto.NotificationItemDTO;
import com.coliving.platform.notification.dto.UnreadSummaryDTO;
import com.coliving.platform.notification.service.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 알림 (API 6-1 ~ 6-4) — 회원 · 관리자 공용 */
@RestController
@RequestMapping("/community-service/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public PageResponse<NotificationItemDTO> getNotifications(
			@RequestHeader("X-User") Long userId,
			@RequestParam(defaultValue = "false") boolean unreadOnly,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return notificationService.getNotifications(userId, unreadOnly, page, size);
	}

	@GetMapping("/unread-summary")
	public UnreadSummaryDTO getUnreadSummary(@RequestHeader("X-User") Long userId) {
		return notificationService.getUnreadSummary(userId);
	}

	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<Void> markRead(@RequestHeader("X-User") Long userId, @PathVariable Long notificationId) {
		notificationService.markRead(userId, notificationId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/read-all")
	public ResponseEntity<Void> markAllRead(@RequestHeader("X-User") Long userId) {
		notificationService.markAllRead(userId);
		return ResponseEntity.noContent().build();
	}
}
