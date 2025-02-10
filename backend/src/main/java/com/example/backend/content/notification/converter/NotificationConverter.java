package com.example.backend.content.notification.converter;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.example.backend.content.notification.dto.NotificationLikePageResponse;
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
			.isRead(notification.isRead())
			.createdAt(notification.getCreateDate())
			.build();
	}

	public NotificationLikePageResponse toLikePage(Page<NotificationLikeResponse> notifications
	) {
		List<NotificationLikeResponse> responses = notifications
			.stream()
			.sorted(Comparator.comparing(NotificationLikeResponse::createdAt).reversed())
			.toList();

		return NotificationLikePageResponse.builder()
			.responses(responses)
			.totalCount((int)notifications.getTotalElements())
			.currentPage(notifications.getNumber())
			.totalPageCount(notifications.getTotalPages())
			.build();
	}

}
