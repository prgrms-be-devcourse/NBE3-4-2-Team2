package com.example.backend.social.follow.dto;

/**
 * 맞팔로우 확인 Response DTO
 * "/mutual/{memberId}" 로 들어오는 맞팔로우 확인 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-07
 */
public record MutualFollowResponse (
	boolean isMutualFollow
) { }
