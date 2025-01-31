package com.example.backend.content.hashtag.exception;

import com.example.backend.global.error.ErrorCodeIfs;
import com.example.backend.global.exception.BackendExceptionIfs;

import lombok.Getter;

@Getter
public class HashtagException extends RuntimeException implements BackendExceptionIfs {

	private final ErrorCodeIfs errorCodeIfs;
	private final String errorDescription;

	public HashtagException(ErrorCodeIfs errorCodeIfs) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();

	}

	public HashtagException(ErrorCodeIfs errorCodeIfs, String errorDescription) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

	public HashtagException(ErrorCodeIfs errorCodeIfs, Throwable tx) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();
	}

	public HashtagException(ErrorCodeIfs errorCodeIfs, Throwable tx, String errorDescription) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

}
