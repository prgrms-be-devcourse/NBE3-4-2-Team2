package com.example.backend.content.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.notification.converter.NotificationConverter;
import com.example.backend.content.notification.exception.NotificationErrorCode;
import com.example.backend.content.notification.exception.NotificationException;
import com.example.backend.content.notification.sse.SseConnectionPool;
import com.example.backend.content.notification.type.NotificationType;
import com.example.backend.entity.NotificationEntity;
import com.example.backend.entity.NotificationRepository;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-09
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationConverter converter;
	private final SseConnectionPool sseConnectionPool;

	@Transactional
	public void createAndSendNotification(Long memberId, Long postId, NotificationType type, String message) {
		// 알림 엔티티 생성 및 저장
		NotificationEntity notificationEntity = NotificationEntity.create(message, memberId, type);
		NotificationEntity notification = notificationRepository.save(notificationEntity);

		// sse 로 실시간 알림 전송
		sseConnectionPool.sendNotification(memberId, converter.toLikeResponse(notification, postId));
	}

	@Transactional
	public void markRead(Long notificationId, Long memberId) {

		NotificationEntity notification = notificationRepository
			.findByIdAndMemberId(notificationId, memberId)
			.orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

		if (notification.isRead()) {
			return;
		}
		notification.markRead();
	}
}
