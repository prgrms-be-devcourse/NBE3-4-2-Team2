package com.example.backend.content.comment.dto;

import java.time.LocalDateTime;

public record CommentResponse(
	Long id,               // 댓글 ID
	String content,        // 댓글 내용
	String username,       // 작성자 이름
	Long postId,           // 게시글 ID
	LocalDateTime createdAt, // 댓글 작성 시간
	int step,              // 댓글 깊이
	int refOrder,            // 같은 그룹 내 정렬 순서
	Long parentNum,
	Long ref
) {
}
