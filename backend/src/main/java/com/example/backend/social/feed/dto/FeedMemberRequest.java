package com.example.backend.social.feed.dto;

import lombok.Builder;

/**
 * 멤버 피드 요청 DTO
 * "/feed/member" 로 들어오는 요청 관련 DTO
 *  처음 요청 시에는 lastPostId에 0 값을 집어넣는다.
 * @author ChoiHyunSan
 * @since 2025-02-07
 */
@Builder
public record FeedMemberRequest(
	Long lastPostId,
	Integer maxSize
) {
}
