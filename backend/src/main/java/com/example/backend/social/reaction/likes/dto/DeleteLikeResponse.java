package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;

import lombok.Builder;

/**
 * 좋아요 취소 Response DTO
 * "/likes" DELETE 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
public record DeleteLikeResponse(
	Long id,
	Long memberId,
	Long postId,
	LocalDateTime deleteDate
) {

	/**
	 * 좋아요 응답 DTO 변환 메서드
	 * LikesEntity 객체를 DeleteLikeResponse DTO 변환
	 *
	 * @param like (LikesEntity)
	 * @return DeleteLikeResponse
	 */
	public static DeleteLikeResponse toResponse(LikesEntity like) {
		return new DeleteLikeResponse(
			like.getId(),
			like.getMember().getId(),
			like.getPost().getId(),
			LocalDateTime.now()
		);
	}
}
