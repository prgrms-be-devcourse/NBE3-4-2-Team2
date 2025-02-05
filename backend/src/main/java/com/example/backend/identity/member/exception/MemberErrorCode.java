package com.example.backend.identity.member.exception;

import org.springframework.http.HttpStatus;

import com.example.backend.global.error.ErrorCodeIfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MemberErrorCode implements ErrorCodeIfs {

	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 401, "비밀번호가 일치하지 않습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, 404, "사용자 정보가 존재하지 않습니다."),
	CONFLICT_RESOURCE(HttpStatus.CONFLICT, 409, "해당 사용자가 이미 존재합니다.");
	// NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 데이터를 찾을 수 없습니다."),


	private final HttpStatus httpStatus;
	private final Integer code;
	private final String description;
}
