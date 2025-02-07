package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 피드 요청 DTO
 * "/feed" 로 들어오는 요청 관련 DTO
 *  처음 요청하는 경우 timestamp 에는 현재 시간을, lastPostId 에는 0 을 넣는다.
 *  이후 요청부터는 Response 로 전달받은 값을 다시 전달하여 다음 게시물을 받는다.
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Builder
public record FeedRequest(
	LocalDateTime timestamp,
	Long lastPostId,
	Integer maxSize,

	// 임시 값
	String username
) {
}
