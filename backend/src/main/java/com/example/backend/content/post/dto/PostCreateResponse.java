package com.example.backend.content.post.dto;

import com.example.backend.entity.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 관련 DTO
 * 게시물 관련 발생하는 응답 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateResponse {

	private Long id;
	private String content;
	private Long memberId;

	/**
	 * PostEntity 객체를 PostCreateResponse로 변환
	 *
	 * @param post 게시물 (PostEntity 객체)
	 * @return PostCreateResponse 객체
	 */

	public static PostCreateResponse fromEntity(PostEntity post) {
		return PostCreateResponse.builder()
			.id(post.getId())
			.content(post.getContent())
			.memberId(post.getMember().getId())
			.build();
	}
}
