package com.example.backend.content.post.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 게시물 관련 예외 클래스
 * 게시물에서 발생하는 예외를 처리하는 클래스
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Getter
public class PostException extends RuntimeException {
	private final PostErrorCode postErrorCode;

	public PostException(PostErrorCode postErrorCode) {
		super(postErrorCode.getMessage());
		this.postErrorCode = postErrorCode;
	}

	public int getStatusCode() {
		return postErrorCode.getHttpStatus().value();
	}

	public String getErrorCode() {
		return postErrorCode.getCode();
	}

	public HttpStatus getHttpStatus() {
		return postErrorCode.getHttpStatus();
	}
}
