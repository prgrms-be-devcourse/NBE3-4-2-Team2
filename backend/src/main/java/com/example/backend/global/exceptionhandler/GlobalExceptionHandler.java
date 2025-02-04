package com.example.backend.global.exceptionhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.backend.global.error.GlobalErrorCode;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.global.rs.ErrorRs;
import com.example.backend.global.rs.RsData;
import com.example.backend.social.reaction.likes.exception.LikesException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kwak
 * Global / 각 도메인 별 Exception 등록
 */

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RsData<List<ErrorRs>>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException ex) {

		log.error("Validation failed", ex);
		BindingResult bindingResult = ex.getBindingResult();
		List<ErrorRs> errorRsList = new ArrayList<>();
		String description = GlobalErrorCode.VALIDATION_FAILED.getDescription();
		Integer code = GlobalErrorCode.VALIDATION_FAILED.getCode();

		for (FieldError error : bindingResult.getFieldErrors()) {
			ErrorRs fieldErrorRs = ErrorRs.builder()
				.target(error.getField())
				.code(code)
				.message(error.getDefaultMessage())
				.build();

			errorRsList.add(fieldErrorRs);
		}

		for (ObjectError error : bindingResult.getGlobalErrors()) {
			ErrorRs objectErrorRs = ErrorRs.builder()
				.target(error.getObjectName())
				.message(error.getDefaultMessage())
				.build();

			errorRsList.add(objectErrorRs);
		}

		return ResponseEntity.badRequest()
			.body(RsData.error(errorRsList, description));

	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<RsData<List<ErrorRs>>> handleConstraintViolationException(
		ConstraintViolationException ex) {

		log.error("Validation failed for method arguments: {}", ex.getMessage(), ex);
		Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
		List<ErrorRs> errorRsList = new ArrayList<>();
		String description = GlobalErrorCode.VALIDATION_FAILED.getDescription();
		Integer code = GlobalErrorCode.VALIDATION_FAILED.getCode();

		for (ConstraintViolation<?> constraintViolation : constraintViolations) {
			ErrorRs constraintErrorRs = ErrorRs.builder()
				.target(constraintViolation.getPropertyPath().toString())
				.code(code)
				.message(constraintViolation.getMessage())
				.build();
			errorRsList.add(constraintErrorRs);
		}

		return ResponseEntity.badRequest()
			.body(RsData.error(errorRsList, description));
	}

	@ExceptionHandler(GlobalException.class)
	public ResponseEntity<RsData<ErrorRs>> handleGlobalException(GlobalException ex,
		HttpServletRequest request) {

		log.error("globalException", ex);

		return ResponseEntity.status(ex.getErrorCodeIfs().getHttpStatus())
			.body(RsData.error(ErrorRs.builder()
				.target(request.getRequestURI())
				.code(ex.getErrorCodeIfs().getCode())
				.message(ex.getErrorDescription())
				.build()));
	}

	@ExceptionHandler(LikesException.class)
	public ResponseEntity<RsData<?>> handleLikesException(LikesException ex) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getStatus())
			.body(response);
	}
}
