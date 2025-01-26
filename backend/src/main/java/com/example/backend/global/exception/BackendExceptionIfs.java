package com.example.backend.global.exception;

import com.example.backend.global.error.ErrorCodeIfs;

public interface BackendExceptionIfs {

	ErrorCodeIfs getErrorCodeIfs();
	String getErrorDescription();

}
