package com.example.backend.social.reaction.bookmark.converter;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkResponse;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkResponse;

public class BookmarkConverter {
	/**
	 * 북마크 응답 DTO 변환 메서드
	 * BookmarkEntity 객체를 CreateBookmarkResponse DTO 변환
	 *
	 * @param bookmark (BookmarkEntity)
	 * @return CreateBookmarkResponse
	 */
	public static CreateBookmarkResponse toCreateResponse(BookmarkEntity bookmark) {
		return new CreateBookmarkResponse(
			bookmark.getId(),
			bookmark.getMember().getId(),
			bookmark.getPost().getId(),
			bookmark.getCreateDate()
		);
	}

	/**
	 * 북마크 응답 DTO 변환 메서드
	 * BookmarkEntity 객체를 DeleteBookmarkResponse DTO 변환
	 *
	 * @param bookmark (BookmarkEntity)
	 * @return DeleteBookmarkResponse
	 */
	public static DeleteBookmarkResponse toDeleteResponse(BookmarkEntity bookmark) {
		return new DeleteBookmarkResponse(
			bookmark.getId(),
			bookmark.getMember().getId(),
			bookmark.getPost().getId(),
			LocalDateTime.now()
		);
	}
}
