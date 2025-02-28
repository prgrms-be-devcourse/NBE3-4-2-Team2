package com.example.backend.social.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SocialErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다."),
	ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "정보를 찾을 수 없습니다."),
	ACTION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
	CANNOT_PERFORM_ON_SELF(HttpStatus.BAD_REQUEST, "자기 자신에게 해당 작업을 수행할 수 없습니다."),
	DATA_MISMATCH(HttpStatus.CONFLICT, "요청한 정보가 일치하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	// 팔로우 관련 오류 메시지
	// CANNOT_FOLLOW_SELF: "자기 자신을 팔로우할 수 없습니다."
	// ALREADY_FOLLOWED: "이미 팔로우 상태입니다."
	// FOLLOW_NOT_FOUND: "팔로우 관계를 찾을 수 없습니다."
	// NO_PERMISSION_TO_UNFOLLOW: "팔로우를 취소할 권한이 없습니다."

	// 좋아요 관련 오류 메시지
	// CANNOT_LIKE_SELF: "자신의 게시물에 좋아요를 누를 수 없습니다."
	// ALREADY_LIKED: "이미 이 게시물에 좋아요를 눌렀습니다."
	// LIKE_NOT_FOUND: "좋아요 정보를 찾을 수 없습니다."
	// NO_PERMISSION_TO_UNLIKE: "좋아요를 취소할 권한이 없습니다."

	// 북마크 관련 오류 메시지
	// ALREADY_BOOKMARKED: "이미 북마크에 추가되어 있습니다."
	// BOOKMARK_NOT_FOUND: "북마크 정보를 찾을 수 없습니다."
	// NO_PERMISSION_TO_ACCESS_BOOKMARK: "북마크에 접근할 권한이 없습니다."
}
