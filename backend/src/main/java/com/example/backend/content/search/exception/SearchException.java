package com.example.backend.content.search.exception;

import com.example.backend.global.error.ErrorCodeIfs;
import com.example.backend.global.exception.BackendExceptionIfs;

import lombok.Getter;

/**
 * @author kwak
 * 2025-02-07
 */
@Getter
public class SearchException extends RuntimeException implements BackendExceptionIfs {

	private final ErrorCodeIfs errorCodeIfs;
	private final String errorDescription;

	public SearchException(ErrorCodeIfs errorCodeIfs) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();

	}

	public SearchException(ErrorCodeIfs errorCodeIfs, String errorDescription) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

	public SearchException(ErrorCodeIfs errorCodeIfs, Throwable tx) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();
	}

	public SearchException(ErrorCodeIfs errorCodeIfs, Throwable tx, String errorDescription) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

}
