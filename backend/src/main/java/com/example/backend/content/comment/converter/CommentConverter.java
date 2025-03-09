package com.example.backend.content.comment.converter;

import com.example.backend.content.comment.dto.CommentCreateResponse;
import com.example.backend.content.comment.dto.CommentDeleteResponse;
import com.example.backend.content.comment.dto.CommentModifyResponse;
import com.example.backend.content.comment.dto.CommentResponse;
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
			.parentNum(comment.getParentNum())
			.ref(comment.getRef())
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

	/**
	 * 댓글 Entity -> 조회 응답 DTO 변환
	 */
	public static CommentResponse toResponse(CommentEntity commentEntity) {
		return new CommentResponse(
			commentEntity.getId(),
			commentEntity.getContent(),
			commentEntity.getMember().getUsername(), // username 추가
			commentEntity.getPost().getId(),
			commentEntity.getCreateDate(), // createDate -> createdAt
			commentEntity.getStep(),
			commentEntity.getRefOrder(),
			commentEntity.getParentNum(),
			commentEntity.getRef()
		);
	}
}
