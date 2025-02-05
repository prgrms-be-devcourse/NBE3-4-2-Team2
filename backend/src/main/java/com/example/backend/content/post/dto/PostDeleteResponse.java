package com.example.backend.content.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 삭제 관련 DTO
 * 게시물 삭제에서 발생하는 응답 관련 DTO
 *
 * @author joonaeng
 * @since 2025-02-03
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostDeleteResponse {
	private Long postId;
	private String message;

	/**
	 * 삭제 성공 응답 생성
	 *
	 * @param postId 삭제한 게시물 ID
	 * @return PostDeleteResponse 객체
	 */
	public static PostDeleteResponse fromEntity(Long postId) {
		return PostDeleteResponse.builder()
			.postId(postId)
			.message("게시물 삭제 성공")
			.build();
	}
}
