package com.example.backend.social.feed.exception;

import com.example.backend.global.error.ErrorCodeIfs;
import com.example.backend.global.exception.BackendExceptionIfs;

import lombok.Getter;

/**
 * FeedException
 * 피드 관련 예외처리를 담당하는 클래스
 *
 * @author ChoiHyunSan
 * @since 2025-02-03
 */
@Getter
public class FeedException extends RuntimeException implements BackendExceptionIfs {

	private final ErrorCodeIfs errorCodeIfs;
	private final String errorDescription;

	public FeedException(ErrorCodeIfs errorCodeIfs) {
		super(errorCodeIfs.getDescription());
		this.errorCodeIfs = errorCodeIfs;
		this.errorDescription = errorCodeIfs.getDescription();
	}
}
