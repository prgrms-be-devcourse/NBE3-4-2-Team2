package com.example.backend.social.reaction.bookmark.converter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.entity.BookmarkEntity;
import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.PostEntity;
import com.example.backend.social.reaction.bookmark.dto.BookmarkListResponse;
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

	public static BookmarkListResponse toBookmarkListResponse(BookmarkEntity bookmark) {
		PostEntity post = bookmark.getPost();

		// 콘텐츠가 너무 길 경우 일부만 잘라서 사용 (예: 첫 50자)
		String contentPreview = post.getContent().length() > 50
			? post.getContent().substring(0, 50) + "..."
			: post.getContent();

		// 이미지 URL 목록 가져오기
		List<String> imageUrls = post.getImages().stream()
			.map(ImageEntity::getImageUrl)
			.collect(Collectors.toList());

		return new BookmarkListResponse(
			bookmark.getId(),
			post.getId(),
			contentPreview,
			imageUrls,
			bookmark.getCreateDate()
		);
	}

	public static List<BookmarkListResponse> toBookmarkListResponseList(List<BookmarkEntity> bookmarks) {
		return bookmarks.stream()
			.map(BookmarkConverter::toBookmarkListResponse)
			.collect(Collectors.toList());
	}
}
