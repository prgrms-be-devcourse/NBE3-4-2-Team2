package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedListResponse {
	private List<FeedInfoResponse> feedList;
	private LocalDateTime lastTimestamp;    // 마지막 피드의 timestamp
	private Long lastPostId;                // 마지막 피드의 id
}
