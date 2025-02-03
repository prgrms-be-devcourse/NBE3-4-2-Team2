package com.example.backend.content.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 수정 관련 DTO
 * 게시물 수정에서 발생하는 요청 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostModifyRequest {

	@NotNull
	private Long postId;

	@NotNull
	private String content;

	@NotNull
	private Long memberId;
}
