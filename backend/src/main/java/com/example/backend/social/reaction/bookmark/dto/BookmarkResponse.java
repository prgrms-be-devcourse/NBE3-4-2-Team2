package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 북마크 응답 DTO
 * "/bookmark"처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-01-31
 */
@Builder
@Getter
@AllArgsConstructor
public class BookmarkResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime createDate;

	/**
	 * 북마크 응답 DTO
	 * BookmarkEntity 객체를 BookmarkResponse DTO 변환
	 *
	 * @param bookmark (변환할 BookmarkEntity 객체)
	 * @return BookmarkResponse
	 */
	public static BookmarkResponse fromEntity(BookmarkEntity bookmark) {
		return BookmarkResponse.builder()
			.id(bookmark.getId())
			.memberId(bookmark.getMember().getId())
			.postId(bookmark.getPost().getId())
			.createDate(bookmark.getCreateDate())
			.build();
	}
}
