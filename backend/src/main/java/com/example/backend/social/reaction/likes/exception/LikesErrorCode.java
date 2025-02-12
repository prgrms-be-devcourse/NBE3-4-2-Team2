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
	MEMBER_MISMATCH(HttpStatus.FORBIDDEN, "좋아요를 취소할 권한이 없습니다."),
	CANNOT_LIKE_SELF(HttpStatus.BAD_REQUEST, "자신의 게시물에 좋아요를 누를 수 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버 정보를 찾을 수 없습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물 정보를 찾을 수 없습니다."),
	LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다."),
	POST_MISMATCH(HttpStatus.CONFLICT, "좋아요 정보와 요청 게시물 정보가 다릅니다."),
	ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다.");

	final HttpStatus httpStatus;
	final String message;
}
