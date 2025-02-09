package com.example.backend.social.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 팔로우 취소 Request DTO
 * "/follow/{receiverId}" 로 들어오는 팔로우 취소 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-07
 */
@Builder
public record DeleteFollowRequest (
	@NotNull(message = "팔로우 Id는 필수 항목입니다.") Long followId
) { }
