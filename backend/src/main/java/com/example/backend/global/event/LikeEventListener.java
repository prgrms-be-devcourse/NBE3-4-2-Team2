package com.example.backend.global.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.backend.content.notification.exception.NotificationErrorCode;
import com.example.backend.content.notification.exception.NotificationException;
import com.example.backend.content.notification.service.NotificationService;
import com.example.backend.content.notification.type.NotificationType;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-09
 */

@Component
@Async
@RequiredArgsConstructor
public class LikeEventListener {

	private final NotificationService notificationService;
	private static final int MAX_RETRY_COUNT = 3;
	private static final long RETRY_DELAY_MS = 1000L;

	@EventListener
	public void handleLikeEvent(LikeEvent likeEvent) {
		int retryCount = 0;

		while (retryCount < MAX_RETRY_COUNT) {

			try {
				notificationService.createAndSendNotification(
					likeEvent.postAuthorId(), likeEvent.postId(), NotificationType.LIKE,
					likeEvent.likerName() + "님이 게시물을 좋아합니다.");
				// 성공 시 바로 리턴
				return;
			} catch (Exception e) {
				retryCount++;
				// 3번째 시도까지 실패 시 진짜 에러 발생
				if (retryCount == MAX_RETRY_COUNT) {
					throw new NotificationException(NotificationErrorCode.FAILED_SEND);
				}

				try {
					// 다음 시도 전 1초 대기
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new NotificationException(NotificationErrorCode.FAILED_SEND);
				}
			}
		}
	}
}
