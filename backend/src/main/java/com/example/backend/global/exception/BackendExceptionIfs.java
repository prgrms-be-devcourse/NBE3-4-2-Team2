package com.example.backend.global.exception;

import com.example.backend.global.error.ErrorCodeIfs;

/**
 * @author kwak
 * 공통으로 사용가능한 BackEndExceptionIfs
 */
public interface BackendExceptionIfs {

	ErrorCodeIfs getErrorCodeIfs();

	String getErrorDescription();

}
