package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 취소 Response DTO
 * "/likes" DELETE 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
@Getter
@AllArgsConstructor
public class DeleteLikeResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime deleteDate;

	/**
	 * 좋아요 응답 DTO 변환 메서드
	 * LikesEntity 객체를 DeleteLikeResponse DTO 변환
	 *
	 * @param like (LikesEntity)
	 * @return DeleteLikeResponse
	 */
	public static DeleteLikeResponse toResponse(LikesEntity like) {
		return DeleteLikeResponse.builder()
			.id(like.getId())
			.memberId(like.getMember().getId())
			.postId(like.getPost().getId())
			.deleteDate(LocalDateTime.now())
			.build();
	}
}
