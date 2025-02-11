package com.example.backend.content.comment.dto;

import com.example.backend.entity.CommentEntity;

public record CommentResponse(
    Long id,
    String content,
    Long memberId,
    Long postId,
    Long parentId,
    boolean isDeleted
) {
    public static CommentResponse fromEntity(CommentEntity comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getMember().getId(),
            comment.getPost().getId(),
            comment.getParentNum(),
            comment.isDeleted()
        );
    }
}
