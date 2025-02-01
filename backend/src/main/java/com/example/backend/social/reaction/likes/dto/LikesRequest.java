package com.example.backend.social.reaction.likes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 요청 DTO
 * "/likes" 로 들어오는 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-01-30
 */
@Builder
@Getter
@AllArgsConstructor
public class LikesRequest {
	@NotNull(message = "Member Id는 필수 항목입니다.")
	private Long memberId;

	@NotNull(message = "Post Id는 필수 항목입니다.")
	private Long postId;
}
