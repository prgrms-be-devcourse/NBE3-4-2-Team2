package com.example.backend.content.post.dto;

import jakarta.validation.constraints.NotNull;
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
public record PostDeleteResponse (
	@NotNull(message = "게시물 ID는 필수 입력 값입니다.") Long postId,
	@NotNull(message = "메시지는 필수 입력 값입니다.") String message
) { }
