package com.example.backend.social.reaction.likes.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 좋아요 예외 코드
 * 좋아요에서 발생하는 예외 코드를 정의하는 Enum 클래스
 *
 * @author Metronon
 * @since 2025-01-30
 */
@AllArgsConstructor
@Getter
public enum LikesErrorCode {
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버 정보를 찾을 수 없습니다.", "404"),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물 정보를 찾을 수 없습니다.", "404"),
	LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다.", "404");

	final HttpStatus httpStatus;
	final String message;
	final String code;
}
