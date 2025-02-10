package com.example.backend.content.notification.converter;

import org.springframework.stereotype.Component;

import com.example.backend.content.notification.dto.NotificationLikeResponse;
import com.example.backend.entity.NotificationEntity;

/**
 * @author kwak
 * 2025-02-10
 */
@Component
public class NotificationConverter {

	public NotificationLikeResponse toLikeResponse(NotificationEntity notification, Long postId
	) {
		return NotificationLikeResponse.builder()
			.notificationId(notification.getId())
			.type(notification.getType())
			.postId(postId)
			.message(notification.getContent())
			.createdAt(notification.getCreateDate())
			.build();
	}
}
