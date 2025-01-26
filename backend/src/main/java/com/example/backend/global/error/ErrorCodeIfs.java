package com.example.backend.global.error;

import org.springframework.http.HttpStatus;

public interface ErrorCodeIfs {

	HttpStatus getHttpStatus();
	Integer getCode();
	String getDescription();

}
