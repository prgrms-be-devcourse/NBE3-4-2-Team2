package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 피드 요청 DTO
 * "/feed" 로 들어오는 요청 관련 DTO
 *
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedRequest {
	private LocalDateTime timestamp;
	private Integer maxSize;
}
