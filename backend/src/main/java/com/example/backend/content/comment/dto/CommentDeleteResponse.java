package com.example.backend.content.comment.dto;

import lombok.Builder;

@Builder
public record CommentDeleteResponse(
	Long id, // 댓글 ID
	Long memberId, // 작성자 ID
	String message // 삭제 메시지
) {}