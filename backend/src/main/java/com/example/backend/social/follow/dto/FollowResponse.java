package com.example.backend.social.follow.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 팔로우 요청 Response DTO
 * "/member/follow/" 로 들어오는 팔로우 요청 관련 응답 DTO
 *
 * @author Metronon
 * @since 2025-03-06
 */
@Builder
public record FollowResponse(
	String senderUsername,
	String receiverUsername,
	LocalDateTime timestamp
) { }
