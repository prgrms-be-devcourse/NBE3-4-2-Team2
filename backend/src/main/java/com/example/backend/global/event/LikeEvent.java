package com.example.backend.global.event;

import java.time.LocalDateTime;

import com.example.backend.content.notification.type.NotificationType;

import lombok.Builder;

/**
 * @author kwak
 * 2025-02-09
 */
@Builder
public record LikeEvent(
	String likerName,
	Long postAuthorId,
	Long postId,
	LocalDateTime timestamp
) {
	public static LikeEvent create(String likerName, Long postAuthorId, Long postId) {
		return LikeEvent.builder()
			.likerName(likerName)
			.postAuthorId(postAuthorId)
			.postId(postId)
			.timestamp(LocalDateTime.now())
			.build();
	}
}
