package com.example.backend.social.feed.exception;

import org.springframework.http.HttpStatus;

import com.example.backend.global.error.ErrorCodeIfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 피드 예외 코드
 * 피드에서 발생하는 예외 코드를 정의하는 Enum 클래스
 *
 * @author ChoiHyunSan
 * @since 2025-02-01
 */
@AllArgsConstructor
@Getter
public enum FeedErrorCode implements ErrorCodeIfs {

	INVALID_TIMESTAMP_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 타임스탬프입니다."),
	INVALID_MAXSIZE_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 범위의 요청 개수입니다."),

	INVALID_POST_REQUEST(HttpStatus.NOT_FOUND, "유효하지 않은 게시물에 대한 피드 요청입니다.");

	private final HttpStatus httpStatus;
	private final String description;
}
