package com.example.backend.social.follow.exception;

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
public enum FollowErrorCode {
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우 할 수 없습니다."),
	CANNOT_UNFOLLOW_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 언팔로우 할 수 없습니다."),
	SENDER_MISMATCH(HttpStatus.FORBIDDEN, "팔로우를 취소할 권한이 없습니다."),
	RECEIVER_MISMATCH(HttpStatus.FORBIDDEN, "잘못된 팔로우 취소 요청입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
	FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다."),
	ALREADY_FOLLOWED(HttpStatus.CONFLICT, "이미 팔로우 상태입니다.");

	final HttpStatus httpStatus;
	final String message;
}
