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
	Long postId,
	LocalDateTime timestamp
) {
	public static CommentEvent create(String commenterName, Long receiverId, Long commentId, Long postId) {
		return CommentEvent.builder()
			.commenterName(commenterName)
			.receiverId(receiverId)
			.commentId(commentId)
			.postId(postId)
			.timestamp(LocalDateTime.now())
			.build();
	}
}
