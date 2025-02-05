package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 북마크 삭제 Response DTO
 * "/bookmark" DELETE 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
@Getter
@AllArgsConstructor
public class DeleteBookmarkResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime deleteDate;

	/**
	 * 북마크 응답 DTO 변환 메서드
	 * BookmarkEntity 객체를 DeleteBookmarkResponse DTO 변환
	 *
	 * @param bookmark (BookmarkEntity)
	 * @return DeleteBookmarkResponse
	 */
	public static DeleteBookmarkResponse toResponse(BookmarkEntity bookmark) {
		return DeleteBookmarkResponse.builder()
			.id(bookmark.getId())
			.memberId(bookmark.getMember().getId())
			.postId(bookmark.getPost().getId())
			.deleteDate(LocalDateTime.now())
			.build();
	}
}
