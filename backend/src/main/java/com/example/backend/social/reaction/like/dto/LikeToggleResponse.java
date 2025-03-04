package com.example.backend.social.reaction.like.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record LikeToggleResponse(
	Long memberId,
	Long resourceId, // 각 Entity 고유 ID
	String resourceType, // post, comment, reply
	boolean isLiked,
	Long likeCount,
	LocalDateTime timestamp
) { }
