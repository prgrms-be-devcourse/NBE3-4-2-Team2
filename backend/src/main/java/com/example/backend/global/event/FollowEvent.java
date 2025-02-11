package com.example.backend.global.event;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * @author kwak
 * 2025-02-11
 */
@Builder
public record FollowEvent(
	String senderName,
	Long receiverId,
	Long senderId,
	LocalDateTime timestamp
) {
	public static FollowEvent create(String senderName, Long receiverId, Long senderId) {
		return FollowEvent.builder()
			.senderName(senderName)
			.receiverId(receiverId)
			.senderId(senderId)
			.timestamp(LocalDateTime.now())
			.timestamp(LocalDateTime.now())
			.build();
	}
}
