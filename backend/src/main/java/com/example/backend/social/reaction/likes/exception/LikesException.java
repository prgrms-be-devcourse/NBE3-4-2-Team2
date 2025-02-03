package com.example.backend.social.reaction.likes.exception;

import org.springframework.http.HttpStatus;

/**
 * 좋아요 예외 처리 클래스
 * 좋아요에서 발생하는 예외를 처리하는 클래스
 *
 * @author Metronon
 * @since 2025-01-30
 */
public class LikesException extends RuntimeException {
	private final LikesErrorCode likesErrorCode;

	public LikesException(LikesErrorCode likesErrorCode) {
		super(likesErrorCode.getMessage());
		this.likesErrorCode = likesErrorCode;
	}

	public HttpStatus getStatus() {
		return likesErrorCode.httpStatus;
	}
}
