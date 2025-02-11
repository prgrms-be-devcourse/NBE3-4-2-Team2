package com.example.backend.content.comment.converter;

import com.example.backend.content.comment.dto.CommentCreateResponse;
import com.example.backend.content.comment.dto.CommentDeleteResponse;
import com.example.backend.content.comment.dto.CommentModifyResponse;
import com.example.backend.entity.CommentEntity;

public class CommentConverter {

	/**
	 * 댓글 Entity -> 생성 응답 DTO 변환
	 */
	public static CommentCreateResponse toCreateResponse(CommentEntity comment) {
		return CommentCreateResponse.builder()
			.id(comment.getId())
			.content(comment.getContent())
			.postId(comment.getPost().getId())
			.memberId(comment.getMember().getId())
			.parentId(comment.getParentNum())
			.build();
	}
	/**
	 * 댓글 Entity -> 수정 응답 DTO 변환
	 */
	public static CommentModifyResponse toModifyResponse(CommentEntity comment) {
		return CommentModifyResponse.builder()
			.id(comment.getId())
			.content(comment.getContent())
			.postId(comment.getPost().getId())
			.memberId(comment.getMember().getId())
			.build();
	}

	/**
	 * 댓글 Entity -> 삭제 응답 DTO 변환
	 */
	public static CommentDeleteResponse toDeleteResponse(Long commentId, Long memberId) {
		return new CommentDeleteResponse(commentId, memberId, "댓글이 삭제되었습니다.");
	}

}
