package com.example.backend.global.event;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * @author kwak
 * 2025-02-11
 */
@Builder
public record CommentEvent(
	String commenterName,
	Long receiverId,
	Long commentId,
	LocalDateTime timestamp
) {
	public static CommentEvent create(String commenterName, Long receiverId, Long commentId) {
		return CommentEvent.builder()
			.commenterName(commenterName)
			.receiverId(receiverId)
			.commentId(commentId)
			.timestamp(LocalDateTime.now())
			.build();
	}
}
