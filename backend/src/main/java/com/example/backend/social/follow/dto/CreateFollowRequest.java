package com.example.backend.social.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 팔로우 요청 Request DTO
 * "/follow/{receiverId}" 로 들어오는 팔로우 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-07
 */
@Builder
public record CreateFollowRequest (
	@NotNull(message = "로그인 후 이용해주세요.") Long senderId,
	@NotNull(message = "팔로우 대상을 확인할 수 없습니다") Long receiverId
) { }

