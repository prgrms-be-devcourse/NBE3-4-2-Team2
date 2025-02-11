package com.example.backend.content.notification.dto;

import java.time.LocalDateTime;

import com.example.backend.content.notification.type.NotificationType;

import lombok.Builder;

/**
 * type
 * LIKE -> PostId, Follow -> senderId, Comment -> commentId
 * @author kwak
 * 2025-02-10
 */
@Builder
public record NotificationResponse(
	Long notificationId,
	NotificationType type,
	Long targetId,
	String message,
	boolean isRead,
	LocalDateTime createdAt
) {
}
