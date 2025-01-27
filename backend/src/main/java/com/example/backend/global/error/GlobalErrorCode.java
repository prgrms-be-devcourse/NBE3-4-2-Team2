package com.example.backend.global.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author kwak
 */

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCodeIfs {

	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 400, "유효성 검증에 실패하였습니다.");

	private final HttpStatus httpStatus;
	private final Integer code;
	private final String description;
}
