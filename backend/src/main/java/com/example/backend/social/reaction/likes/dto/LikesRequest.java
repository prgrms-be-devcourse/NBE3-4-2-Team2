package com.example.backend.social.reaction.likes.dto;

import java.time.LocalDateTime;

import com.example.backend.entity.LikesEntity;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;

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
	private Long memberId;
	private Long postId;

	/**
	 * LikesRequest DTO 를 LikesEntity 객체로 변환
	 *
	 * @param member (memberEntity 객체)
	 * @param post (postEntity 객체)
	 * @return LikesEntity
	 */
	public LikesEntity toEntity(MemberEntity member, PostEntity post) {
		return new LikesEntity(member, post, LocalDateTime.now());
	}
}
