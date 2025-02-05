package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 적용 Response DTO
 * "/likes" POST 처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-02-04
 */
@Builder
@Getter
@AllArgsConstructor
public class CreateLikeResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime createDate;

	/**
	 * 좋아요 응답 DTO 변환 메서드
	 * LikesEntity 객체를 CreateLikeResponse DTO 변환
	 *
	 * @param like (LikesEntity)
	 * @return CreateLikeResponse
	 */
	public static CreateLikeResponse toResponse(LikesEntity like) {
		return CreateLikeResponse.builder()
			.id(like.getId())
			.memberId(like.getMember().getId())
			.postId(like.getPost().getId())
			.createDate(like.getCreateDate())
			.build();
	}
}

