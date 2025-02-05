package com.example.backend.social.reaction.bookmark.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 북마크 생성 Request DTO
 * "/bookmark" 로 들어오는 생성 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
@Getter
@AllArgsConstructor
public class CreateBookmarkRequest {
	@NotNull(message = "Member Id는 필수 항목입니다.")
	private Long memberId;

	@NotNull(message = "Post Id는 필수 항목입니다.")
	private Long postId;
}
