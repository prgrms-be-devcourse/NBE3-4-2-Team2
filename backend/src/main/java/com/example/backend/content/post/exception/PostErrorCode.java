package com.example.backend.content.post.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 게시물 관련 예외 코드
 * 게시물에서 발생하는 예외 코드를 정의하는 Enum 클래스
 *
 * @author joonaeng
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public enum PostErrorCode {

	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다.", "404"),
	POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "게시물을 수정할 권한이 없습니다.", "403"),
	POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "게시물을 삭제할 권한이 없습니다.", "403");

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
