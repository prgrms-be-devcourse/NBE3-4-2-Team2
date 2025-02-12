package com.example.backend.content.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommentModifyRequest(
        @NotNull(message = "댓글 ID는 필수 입력 값입니다.") Long commentId,
        @NotNull(message = "회원 ID는 필수 입력 값입니다.") Long memberId,
        @NotNull(message = "수정할 내용은 필수 입력 값입니다.") String content
) {}