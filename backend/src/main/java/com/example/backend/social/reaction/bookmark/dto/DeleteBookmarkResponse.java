package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class DeleteBookmarkResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime deleteDate;

	/**
	 * 북마크 응답 DTO
	 * BookmarkEntity 객체를 BookmarkResponse DTO 변환
	 *
	 * @param bookmark (변환할 BookmarkEntity 객체)
	 * @return BookmarkResponse
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
