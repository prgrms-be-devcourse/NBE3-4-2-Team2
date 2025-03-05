package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 북마크 생성 Response DTO
 * "/bookmark" POST 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record CreateBookmarkResponse(
	Long bookmarkId,
	Long memberId,
	Long postId,
	LocalDateTime createDate
) { }
