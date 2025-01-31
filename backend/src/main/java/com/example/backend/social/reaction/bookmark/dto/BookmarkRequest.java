package com.example.backend.social.reaction.bookmark.dto;

import com.example.backend.entity.BookmarkEntity;

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

	/**
	 * BookmarkRequest DTO 를 BookmarkEntity 객체로 변환
	 *
	 * @param member (memberEntity 객체)
	 * @param post (postEntity 객체)
	 * @return BookmarkEntity
	 */
	public static BookmarkEntity toEntity(BookmarkEntity bookmark) {
		return BookmarkEntity.builder()
			.id(bookmark.getId())
			.memberId(bookmark.getMember().getId())
			.postId(bookmark.getPost().getId())
			.createDate(bookmark.getCreateDate())
			.build();
	}
}
