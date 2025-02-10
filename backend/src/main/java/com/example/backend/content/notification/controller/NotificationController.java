package com.example.backend.content.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.backend.content.notification.sse.SseConnection;
import com.example.backend.content.notification.sse.SseConnectionPool;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-09
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

	private final SseConnectionPool sseConnectionPool;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe() {
		// 시큐리티 인증 작업 전까지 임시로 진행
		String uniqueKey = "userId1";
		SseConnection connection = SseConnection.connect(uniqueKey, sseConnectionPool);

		return connection.getSseEmitter();
	}
}
