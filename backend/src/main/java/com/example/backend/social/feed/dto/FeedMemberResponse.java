package com.example.backend.social.feed.dto;

import java.util.List;

import lombok.Builder;

/**
 * 멤버 피드 반환 DTO
 * "/feed/member" 로 들어오는 요청을 반환하는 DTO
 *
 * @author ChoiHyunSan
 * @since 2025-02-07
 */
@Builder
public record FeedMemberResponse(
	List<FeedInfoResponse> feedList,
	Long lastPostId                // 마지막 피드의 id
) {

	public static FeedMemberResponse create(List<FeedInfoResponse> feedList, Long lastPostId) {
		return FeedMemberResponse.builder()
			.feedList(feedList)
			.lastPostId(lastPostId)
			.build();
	}
}
