package com.example.backend.content.comment.dto;

import lombok.Builder;

@Builder
public record CommentCreateResponse(
		Long id,
		String content,
        Long postId,
		Long memberId,
		Long parentId
) {}