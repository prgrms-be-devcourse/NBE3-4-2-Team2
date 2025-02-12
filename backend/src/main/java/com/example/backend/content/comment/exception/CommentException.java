package com.example.backend.content.comment.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CommentException extends RuntimeException {

	private final CommentErrorCode commentErrorCode;
	private final HttpStatus httpStatus;

	public CommentException(CommentErrorCode errorCode) {
		super(errorCode.getMessage());
		this.commentErrorCode = errorCode;
		this.httpStatus = errorCode.getStatus();
	}
}