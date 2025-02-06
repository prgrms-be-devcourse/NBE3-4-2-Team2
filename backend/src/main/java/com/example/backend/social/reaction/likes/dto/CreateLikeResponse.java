package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;

import lombok.Builder;

/**
 * 좋아요 적용 Response DTO
 * "/likes" POST 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record CreateLikeResponse(
	Long id,
	Long memberId,
	Long postId,
	LocalDateTime createDate
) {

	/**
	 * 좋아요 응답 DTO 변환 메서드
	 * LikesEntity 객체를 CreateLikeResponse DTO 변환
	 *
	 * @param like (LikesEntity)
	 * @return CreateLikeResponse
	 */
	public static CreateLikeResponse toResponse(LikesEntity like) {
		return new CreateLikeResponse(
			like.getId(),
			like.getMember().getId(),
			like.getPost().getId(),
			like.getCreateDate()
		);
	}
}

