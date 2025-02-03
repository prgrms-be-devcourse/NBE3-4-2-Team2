package com.example.backend.content.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시물 삭제 관련 DTO
 * 게시물 삭제에서 발생하는 요청 관련 DTO
 *
 * @author joonaeng
 * @since 2025-02-03
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostDeleteRequest {

	@NotNull
	private Long postId;

	@NotNull
	private Long memberId;

}
