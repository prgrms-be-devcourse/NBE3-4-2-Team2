package com.example.backend.global.exception;

import com.example.backend.global.error.ErrorCodeIfs;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException implements BackendExceptionIfs {

	private final ErrorCodeIfs errorCodeIfs;
	private final String errorDescription;

	public GlobalException(ErrorCodeIfs errorCodeIfs) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();

	}

	public GlobalException(ErrorCodeIfs errorCodeIfs, String errorDescription) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

	public GlobalException(ErrorCodeIfs errorCodeIfs, Throwable tx) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();
	}

	public GlobalException(ErrorCodeIfs errorCodeIfs, Throwable tx, String errorDescription) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

}
