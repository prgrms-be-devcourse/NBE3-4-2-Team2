package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.BookmarkEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 북마크 생성 Response DTO
 * "/bookmark" POST 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
@Getter
@AllArgsConstructor
public class CreateBookmarkResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime createDate;

	/**
	 * 북마크 응답 DTO 변환 메서드
	 * BookmarkEntity 객체를 CreateBookmarkResponse DTO 변환
	 *
	 * @param bookmark (BookmarkEntity)
	 * @return CreateBookmarkResponse
	 */
	public static CreateBookmarkResponse toResponse(BookmarkEntity bookmark) {
		return CreateBookmarkResponse.builder()
			.id(bookmark.getId())
			.memberId(bookmark.getMember().getId())
			.postId(bookmark.getPost().getId())
			.createDate(bookmark.getCreateDate())
			.build();
	}
}
