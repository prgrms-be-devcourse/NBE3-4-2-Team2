package com.example.backend.social.follow.exception;

import org.springframework.http.HttpStatus;

/**
 * 좋아요 예외 처리 클래스
 * 좋아요에서 발생하는 예외를 처리하는 클래스
 *
 * @author Metronon
 * @since 2025-01-30
 */
public class FollowException extends RuntimeException {
	private final FollowErrorCode followErrorCode;

	public FollowException(FollowErrorCode followErrorCode) {
		super(followErrorCode.getMessage());
		this.followErrorCode = followErrorCode;
	}

	public HttpStatus getStatus() {
		return followErrorCode.httpStatus;
	}
}
