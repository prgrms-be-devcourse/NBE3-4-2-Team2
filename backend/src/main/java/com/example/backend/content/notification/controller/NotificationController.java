package com.example.backend.content.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.backend.content.notification.dto.NotificationPageResponse;
import com.example.backend.content.notification.service.NotificationService;
import com.example.backend.content.notification.sse.SseConnection;
import com.example.backend.content.notification.sse.SseConnectionPool;
import com.example.backend.global.rs.RsData;
import com.example.backend.identity.security.user.CustomUser;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-09
 */
@RestController
@RequestMapping("/api-v1/notification")
@RequiredArgsConstructor
public class NotificationController {

	private final SseConnectionPool sseConnectionPool;
	private final NotificationService notificationService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(
		@AuthenticationPrincipal CustomUser customUser,
		@RequestParam(name = "browserName", defaultValue = "unknown") String browserName) {
		SseConnection connection = SseConnection.connect(
			String.valueOf(customUser.getId()), browserName, sseConnectionPool);

		return connection.getSseEmitter();
	}

	@PutMapping("/{notificationId}/read")
	@ResponseStatus(HttpStatus.OK)
	public RsData<Void> markAsRead(
		@AuthenticationPrincipal CustomUser customUser,
		@PathVariable Long notificationId
	) {

		notificationService.markRead(notificationId, customUser.getId());
		return RsData.success(null);
	}

	/**
	 * 좋아요,팔로잉,댓글 등 타입 가리지 않고 알림 가져오기
	 * @author kwak
	 * @since 2025-02-10
	 */
	@GetMapping("/list")
	@ResponseStatus(HttpStatus.OK)
	public RsData<NotificationPageResponse> list(
		@RequestParam(name = "page", defaultValue = "0") int page,
		@AuthenticationPrincipal CustomUser customUser
	) {
		NotificationPageResponse notificationPage =
			notificationService.getNotificationPage(page, customUser.getId());

		return RsData.success(notificationPage);
	}
}
