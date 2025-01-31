package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 좋아요 응답 DTO
 * "/likes"처리 후 응답 관련 DTO
 *
 * @author Metronon
 * @since 2025-01-30
 */
@Builder
@Getter
@AllArgsConstructor
public class LikesResponse {
	private Long id;
	private Long memberId;
	private Long postId;
	private LocalDateTime createDate;

	/**
	 * 좋아요 응답 DTO
	 * LikesEntity 객체를 LikesResponse DTO 변환
	 *
	 * @param likes (변환할 LikesEntity 객체)
	 * @return LikesResponse
	 */
	public static LikesResponse fromEntity(LikesEntity likes) {
		return LikesResponse.builder()
			.id(likes.getId())
			.memberId(likes.getMember().getId())
			.postId(likes.getPost().getId())
			.createDate(likes.getCreateDate())
			.build();
	}
}
