package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;

import lombok.Builder;

/**
 * 북마크 생성 Response DTO
 * "/bookmark" POST 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
public record CreateBookmarkResponse(
	Long id,
	Long memberId,
	Long postId,
	LocalDateTime createDate
) {
	/**
	 * 북마크 응답 DTO 변환 메서드
	 * BookmarkEntity 객체를 CreateBookmarkResponse DTO 변환
	 *
	 * @param bookmark (BookmarkEntity)
	 * @return CreateBookmarkResponse
	 */
	@Builder
	public static CreateBookmarkResponse toResponse(BookmarkEntity bookmark) {
		return new CreateBookmarkResponse(
			bookmark.getId(),
			bookmark.getMember().getId(),
			bookmark.getPost().getId(),
			bookmark.getCreateDate()
		);
	}
}
