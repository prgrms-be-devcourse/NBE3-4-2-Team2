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
public record PostModifyRequest (
	@NotNull(message = "게시물 ID는 필수 입력 값입니다.") Long postId,
	@NotNull(message = "게시물 내용은 필수 입력 값입니다.") String content,
	@NotNull(message = "회원 ID는 필수 입력 값입니다.") Long memberId
) { }
