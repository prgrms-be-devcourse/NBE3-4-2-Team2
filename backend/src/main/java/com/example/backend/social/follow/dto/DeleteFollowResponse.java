package com.example.backend.social.follow.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 팔로우 취소 Response DTO
 * "/follow/{receiverId}" 로 들어오는 팔로우 취소 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-07
 */
@Builder
public record DeleteFollowResponse (
	Long id,
	Long senderId,
	Long receiverId,
	LocalDateTime deleteDate
) { }
