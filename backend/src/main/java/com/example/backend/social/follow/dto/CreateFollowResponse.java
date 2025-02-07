package com.example.backend.social.follow.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 팔로우 요청 Response DTO
 * "/{receiverId}/follow" 로 들어오는 팔로우 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-07
 */
@Builder
public record CreateFollowResponse (
	Long id,
	Long memberId,
	Long postId,
	LocalDateTime createDate
) { }
