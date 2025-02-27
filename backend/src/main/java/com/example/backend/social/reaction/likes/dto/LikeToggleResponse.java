package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record LikeToggleResponse(
	Long resourceId, // 각 Entity 고유 ID
	Long memberId,
	String resourceType, // 게시물, 댓글, 대댓글
	boolean isLiked,
	Long likeCount,
	LocalDateTime timestamp
) { }
