package com.example.backend.content.comment.dto;

import lombok.Builder;

@Builder
public record CommentModifyResponse(
	Long id, // 댓글 ID
	String content, // 댓글 내용
	Long postId, // 게시물 ID
	Long memberId // 작성자 ID
) {}