package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 좋아요 적용 Response DTO
 * "/likes" POST 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record CreateLikeResponse(
	Long id,
	Long memberId,
	Long postId,
	LocalDateTime createDate
) { }

