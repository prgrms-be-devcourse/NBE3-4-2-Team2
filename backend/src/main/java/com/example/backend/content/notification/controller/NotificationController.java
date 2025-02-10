package com.example.backend.content.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.backend.content.notification.service.NotificationService;
import com.example.backend.content.notification.sse.SseConnection;
import com.example.backend.content.notification.sse.SseConnectionPool;
import com.example.backend.global.rs.RsData;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-09
 */
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

	private final SseConnectionPool sseConnectionPool;
	private final NotificationService notificationService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe() {
		// 시큐리티 인증 작업 전까지 임시로 진행
		String uniqueKey = "userId1";
		SseConnection connection = SseConnection.connect(uniqueKey, sseConnectionPool);

		return connection.getSseEmitter();
	}

	@PutMapping("/{notificationId}/read")
	public RsData<Void> markAsRead(
		@PathVariable Long notificationId
	) {
		// 시큐리티 인증 작업 전까지 임시로 진행
		Long userId = 777L;
		notificationService.markRead(notificationId, userId);
		return RsData.success(null);
	}
}
