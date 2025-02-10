package com.example.backend.content.notification.dto;

import java.time.LocalDateTime;

import com.example.backend.content.notification.type.NotificationType;

import lombok.Builder;

/**
 * @author kwak
 * 2025-02-10
 */
@Builder
public record NotificationLikeResponse(
	Long notificationId,
	NotificationType type,
	Long postId,
	String message,
	boolean isRead,
	LocalDateTime createdAt
) {
}
