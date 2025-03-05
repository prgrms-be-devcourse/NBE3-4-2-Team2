package com.example.backend.content.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 게시물 생성 관련 DTO
 * 게시물 생성에서 발생하는 응답 관련 DTO
 *
 * @author joonaeng
 * @since 2025-01-31
 */

@Builder
public record PostCreateResponse (
	@NotNull(message = "게시물 번호는 필수 입력 값입니다.") Long id,
	@NotNull(message = "게시물 내용은 필수 입력 값입니다.") String content,
	@NotNull(message = "회원 번호는 필수 입력 값입니다.") Long memberId
){ }
