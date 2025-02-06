package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;

import lombok.Builder;

/**
 * 북마크 삭제 Response DTO
 * "/bookmark" DELETE 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record DeleteBookmarkResponse(
	Long id,
	Long memberId,
	Long postId,
	LocalDateTime deleteDate
) {
	/**
	 * 북마크 응답 DTO 변환 메서드
	 * BookmarkEntity 객체를 DeleteBookmarkResponse DTO 변환
	 *
	 * @param bookmark (BookmarkEntity)
	 * @return DeleteBookmarkResponse
	 */
	public static DeleteBookmarkResponse toResponse(BookmarkEntity bookmark) {
		return new DeleteBookmarkResponse(
			bookmark.getId(),
			bookmark.getMember().getId(),
			bookmark.getPost().getId(),
			LocalDateTime.now()
		);
	}
}
