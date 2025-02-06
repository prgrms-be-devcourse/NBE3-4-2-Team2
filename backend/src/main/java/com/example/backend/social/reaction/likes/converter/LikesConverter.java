package com.example.backend.social.reaction.likes.converter;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;
import com.example.backend.social.reaction.likes.dto.CreateLikeResponse;
import com.example.backend.social.reaction.likes.dto.DeleteLikeResponse;

public class LikesConverter {
	/**
	 * 좋아요 응답 DTO 변환 메서드
	 * LikesEntity 객체를 CreateLikeResponse DTO 변환
	 *
	 * @param like (LikesEntity)
	 * @return CreateLikeResponse
	 */
	public static CreateLikeResponse toCreateResponse(LikesEntity like) {
		return new CreateLikeResponse(
			like.getId(),
			like.getMember().getId(),
			like.getPost().getId(),
			like.getCreateDate()
		);
	}

	/**
	 * 좋아요 응답 DTO 변환 메서드
	 * LikesEntity 객체를 DeleteLikeResponse DTO 변환
	 *
	 * @param like (LikesEntity)
	 * @return DeleteLikeResponse
	 */
	public static DeleteLikeResponse toDeleteResponse(LikesEntity like) {
		return new DeleteLikeResponse(
			like.getId(),
			like.getMember().getId(),
			like.getPost().getId(),
			LocalDateTime.now()
		);
	}
}
