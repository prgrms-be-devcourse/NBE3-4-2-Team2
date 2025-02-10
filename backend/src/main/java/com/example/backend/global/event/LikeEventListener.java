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

	@EventListener
	public void handleLikeEvent(LikeEvent likeEvent) {
		try {
			notificationService.createAndSendNotification(
				likeEvent.postAuthorId(), likeEvent.postId(), NotificationType.LIKE,
				likeEvent.likerName() + "님이 게시물을 좋아합니다.");
		} catch (Exception e) {
			throw new NotificationException(NotificationErrorCode.FAILED_SEND);
		}
	}
}
