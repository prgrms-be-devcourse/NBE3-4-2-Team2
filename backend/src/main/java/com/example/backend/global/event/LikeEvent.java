package com.example.backend.global.event;

import java.time.LocalDateTime;

/**
 * @author kwak
 * 2025-02-09
 */
public record LikeEvent(
	String likerName,
	Long resourceOwnerId,
	Long resourceId,
	String resourceType,
	LocalDateTime timestamp
) {
	public static LikeEvent create(String likerName, Long resourceOwnerId, Long resourceId, String resourceType) {
		return new LikeEvent(
			likerName,
			resourceOwnerId,
			resourceId,
			resourceType,
			LocalDateTime.now()
		);
	}
}
