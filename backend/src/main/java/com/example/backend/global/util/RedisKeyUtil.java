package com.example.backend.global.util;

public class RedisKeyUtil {
	private static final String LIKE_KEY_FORMAT = "like:%s:%d:%d";
	private static final String LIKE_COUNT_KEY_FORMAT = "likeCount:%s:%d";

	public static String getLikeKey(String resourceType, Long resourceId, Long memberId) {
		return String.format(LIKE_KEY_FORMAT, resourceType, resourceId, memberId);
	}

	public static String getLikeCountKey(String resourceType, Long resourceId) {
		return String.format(LIKE_COUNT_KEY_FORMAT, resourceType, resourceId);
	}
}
