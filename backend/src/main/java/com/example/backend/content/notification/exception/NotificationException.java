package com.example.backend.content.notification.exception;

import com.example.backend.global.error.ErrorCodeIfs;
import com.example.backend.global.exception.BackendExceptionIfs;

import lombok.Getter;

/**
 * @author kwak
 * 2025-02-10
 */
@Getter
public class NotificationException extends RuntimeException implements BackendExceptionIfs {

	private final ErrorCodeIfs errorCodeIfs;
	private final String errorDescription;

	public NotificationException(ErrorCodeIfs errorCodeIfs) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();

	}

	public NotificationException(ErrorCodeIfs errorCodeIfs, String errorDescription) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

	public NotificationException(ErrorCodeIfs errorCodeIfs, Throwable tx) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();
	}

	public NotificationException(ErrorCodeIfs errorCodeIfs, Throwable tx, String errorDescription) {
		super(tx);
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorDescription;
	}

}

