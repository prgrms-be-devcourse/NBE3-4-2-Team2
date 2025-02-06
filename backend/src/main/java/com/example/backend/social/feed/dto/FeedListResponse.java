package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

/**
 * 피드 리스트 DTO
 * 요청한 피드 리스트와 커서 페이징을 위한 정보 (timestamp & lastPostId)를 담은 DTO
 *
 * @author ChoiHyunSan
 * @since 2025-02-03
 */
@Builder
public record FeedListResponse(
	List<FeedInfoResponse> feedList,
	LocalDateTime lastTimestamp,    // 마지막 피드의 timestamp
	Long lastPostId                // 마지막 피드의 id
) {
}
