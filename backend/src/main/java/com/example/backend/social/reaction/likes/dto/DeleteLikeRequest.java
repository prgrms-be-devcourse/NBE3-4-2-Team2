package com.example.backend.social.reaction.likes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 취소 Request DTO
 * "/likes" 로 들어오는 삭제 요청 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
@Getter
@AllArgsConstructor
public class DeleteLikeRequest {
	@NotNull(message = "좋아요 Id는 필수 항목입니다.")
	private Long id;

	@NotNull(message = "Member Id는 필수 항목입니다.")
	private Long memberId;

	@NotNull(message = "Post Id는 필수 항목입니다.")
	private Long postId;
}
