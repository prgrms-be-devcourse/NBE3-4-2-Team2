package com.example.backend.social.reaction.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 북마크 요청 DTO
 * "/bookmark" 로 들어오는 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-01-31
 */
@Builder
@Getter
@AllArgsConstructor
public class BookmarkRequest {
	private Long memberId;
	private Long postId;
}
