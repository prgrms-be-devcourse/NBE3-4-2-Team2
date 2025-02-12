package com.example.backend.global.error;

import org.springframework.http.HttpStatus;

/**
 * @author kwak
 * 공통으로 사용가능한 ErrorCodeIfs
 */
public interface ErrorCodeIfs {

	HttpStatus getHttpStatus();

	String getDescription();

}
