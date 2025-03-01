package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 좋아요 취소 Response DTO
 * "/likes" DELETE 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record DeleteLikeResponse(
	Long likeId,
	Long memberId,
	Long postId,
	LocalDateTime deleteDate
) { }
