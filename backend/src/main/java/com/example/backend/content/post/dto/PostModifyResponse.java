package com.example.backend.content.post.dto;

import com.example.backend.entity.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 수정 관련 DTO
 * 게시물 수정에서 발생하는 응답 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostModifyResponse {

	private Long id;
	private String content;
	private Long memberId;

	/**
	 * PostEntity 객체를 PostModifyResponse로 변환
	 * 수정된 내용을 전달
	 *
	 * @param post 게시물 (PostEntity 객체)
	 * @return PostModifyResponse 객체
	 */
	public static PostModifyResponse fromEntity(PostEntity post) {
		return PostModifyResponse.builder()
			.id(post.getId())
			.content(post.getContent())
			.memberId(post.getMember().getId())
			.build();
	}
}
