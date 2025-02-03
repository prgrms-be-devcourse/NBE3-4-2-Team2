package com.example.backend.content.post.dto;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 생성 관련 DTO
 * 게시물 생성에서 발생하는 요청 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateRequest {

	private Long memberId;
	private String content;

	/**
	 * PostCreateRequest를 PostEntity 객체로 변환
	 *
	 * @param memberEntity 게시물 작성자 (MemberEntity 객체)
	 * @return PostEntity 객체
	 */
	public PostEntity toEntity(MemberEntity memberEntity) {
		return PostEntity.builder()
			.content(content)
			.member(memberEntity)
			.isDeleted(false)
			.build();
	}
}
