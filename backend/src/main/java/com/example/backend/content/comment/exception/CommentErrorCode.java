package com.example.backend.content.comment.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum CommentErrorCode {

	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "댓글 수정 권한이 없습니다."),
	COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다."),
	PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다.");

	private final HttpStatus status;
	private final String message;

	CommentErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}