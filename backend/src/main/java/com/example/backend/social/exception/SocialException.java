package com.example.backend.social.exception;

import org.springframework.http.HttpStatus;

public class SocialException extends RuntimeException {
	private final SocialErrorCode errorCode;

	public SocialException(SocialErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public SocialException(SocialErrorCode errorCode, String customMessage) {
		super(customMessage);
		this.errorCode = errorCode;
	}

	public HttpStatus getStatus() {
		return errorCode.getHttpStatus();
	}

	public SocialErrorCode getErrorCode() {
		return this.errorCode;
	}
}
