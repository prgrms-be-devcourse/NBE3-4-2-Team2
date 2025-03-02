package com.example.backend.content.notification.converter;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.example.backend.content.notification.dto.NotificationPageResponse;
import com.example.backend.content.notification.dto.NotificationResponse;
import com.example.backend.entity.NotificationEntity;

/**
 * @author kwak
 * 2025-02-10
 */
@Component
public class NotificationConverter {

	public NotificationResponse toResponse(NotificationEntity notification, Long targetId
	) {
		return NotificationResponse.builder()
			.notificationId(notification.getId())
			.type(notification.getType())
			.targetId(targetId)
			.message(notification.getContent())
			.isRead(notification.isRead())
			.createdAt(notification.getCreateDate())
			.build();
	}

	public NotificationPageResponse toPageResponse(Page<NotificationResponse> notifications
	) {
		List<NotificationResponse> responses = notifications
			.stream()
			.sorted(Comparator.comparing(NotificationResponse::createdAt).reversed())
			.toList();

		return NotificationPageResponse.builder()
			.responses(responses)
			.totalCount((int)notifications.getTotalElements())
			.currentPage(notifications.getNumber())
			.totalPageCount(notifications.getTotalPages())
			.build();
	}

}
