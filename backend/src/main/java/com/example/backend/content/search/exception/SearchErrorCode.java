package com.example.backend.content.search.exception;

import org.springframework.http.HttpStatus;

import com.example.backend.global.error.ErrorCodeIfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author kwak
 * 2025-02-07
 */
@Getter
@AllArgsConstructor
public enum SearchErrorCode implements ErrorCodeIfs {

	INVALID_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "검색 타입이 잘못됐습니다.");

	private final HttpStatus httpStatus;
	private final String description;

}
