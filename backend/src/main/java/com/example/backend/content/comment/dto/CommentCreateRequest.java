package com.example.backend.content.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommentCreateRequest(
	@NotNull(message = "게시물 ID는 필수 입력 값입니다.") Long postId,
	@NotNull(message = "회원 ID는 필수 입력 값입니다.") Long memberId,
	@NotNull(message = "댓글 내용은 필수 입력 값입니다.") String content,
	Long parentNum // 부모 댓글 ID (없으면 원댓글)
) {
}