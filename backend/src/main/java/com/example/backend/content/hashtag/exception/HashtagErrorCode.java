package com.example.backend.content.hashtag.exception;

import org.springframework.http.HttpStatus;

import com.example.backend.global.error.ErrorCodeIfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HashtagErrorCode implements ErrorCodeIfs {

	INVALID_HASHTAG_CONTENT(HttpStatus.BAD_REQUEST, 400, "해시태그 특수문자를 포함할 수 없습니다."),
	EMPTY_CONTENT(HttpStatus.BAD_REQUEST, 400, "해시태그 내용이 없습니다."),
	TOO_LONG_HASHTAG_CONTENT(HttpStatus.BAD_REQUEST, 400, "해시태그 길이가 10자를 초과합니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 데이터를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final Integer code;
	private final String description;
}
