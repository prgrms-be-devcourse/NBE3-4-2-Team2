package com.example.backend.global.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.backend.content.notification.exception.NotificationErrorCode;
import com.example.backend.content.notification.exception.NotificationException;
import com.example.backend.content.notification.service.NotificationService;
import com.example.backend.content.notification.type.NotificationType;
import com.example.backend.entity.NotificationEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kwak
 * 2025-02-09
 */

@Component
@Async
@RequiredArgsConstructor
@Slf4j
public class LikeEventListener {

	private final NotificationService notificationService;
	private static final int MAX_RETRY_COUNT = 3;
	private static final long RETRY_DELAY_MS = 1000L;

	@EventListener
	public void handleLikeEvent(LikeEvent likeEvent) {
		int retryCount = 0;

		// 리소스 타입에 따른 컨텐츠 이름 설정
		String resourceName = getResourceName(likeEvent.resourceType());

		// 알림 메시지 생성
		String message = likeEvent.likerName() + "님이 당신의 " + resourceName + "에 좋아요를 눌렀습니다.";

		// 알림 저장
		NotificationEntity notification = notificationService.createNotification(
			likeEvent.resourceOwnerId(),
			likeEvent.resourceId(),
			NotificationType.LIKE,
			message);

		while (retryCount < MAX_RETRY_COUNT) {
			try {
				// 알림 전송
				notificationService.sendNotification(likeEvent.resourceOwnerId(), notification);
				// 성공 시 바로 리턴
				return;
			} catch (Exception e) {
				retryCount++;
				// 3번째 시도까지 실패 시 실제 에러 발생
				if (retryCount == MAX_RETRY_COUNT) {
					throw new NotificationException(NotificationErrorCode.FAILED_SEND, e);
				}

				try {
					// 다음 시도 전 1초 대기
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new NotificationException(NotificationErrorCode.FAILED_SEND, ie);
				}
			}
		}
	}

	// 리소스 타입에 따른 컨텐츠 이름 반환 메서드
	private String getResourceName(String resourceType) {
		return switch (resourceType.toUpperCase()) {
			case "POST" -> "게시물";
			case "COMMENT" -> "댓글";
			case "REPLY" -> "대댓글";
			default -> "콘텐츠";
		};
	}
}
