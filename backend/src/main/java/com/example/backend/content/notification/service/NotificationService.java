package com.example.backend.content.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.notification.converter.NotificationConverter;
import com.example.backend.content.notification.dto.NotificationLikePageResponse;
import com.example.backend.content.notification.dto.NotificationLikeResponse;
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

	private static final long THIRTY_DAYS = 30;
	private static final int PAGE_SIZE = 10;

	@Transactional
	public void createAndSendNotification(Long memberId, Long postId, NotificationType type, String message) {
		// 알림 엔티티 생성 및 저장
		NotificationEntity notificationEntity = NotificationEntity.create(message, memberId, type, postId);
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

	public NotificationLikePageResponse getNotificationPage(int page, Long memberId) {
		PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdDate"));
		// 알림 목록 조회 (최근 30일)
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(THIRTY_DAYS);

		Page<NotificationEntity> notifications =
			notificationRepository.findByMemberId(memberId, thirtyDaysAgo, pageRequest);

		Page<NotificationLikeResponse> likeResponse = notifications.map(
			notification -> converter.toLikeResponse(notification, notification.getTargetId())
		);

		return converter.toLikePage(likeResponse);
	}
}
