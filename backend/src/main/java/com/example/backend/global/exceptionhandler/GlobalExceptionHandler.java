package com.example.backend.global.exceptionhandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.backend.content.hashtag.exception.HashtagException;
import com.example.backend.content.post.exception.PostException;
import com.example.backend.content.search.exception.SearchException;
import com.example.backend.global.error.GlobalErrorCode;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.global.rs.ErrorRs;
import com.example.backend.global.rs.RsData;
import com.example.backend.social.feed.exception.FeedException;
import com.example.backend.social.reaction.bookmark.exception.BookmarkException;
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
		Integer code = GlobalErrorCode.VALIDATION_FAILED.getHttpStatus().value();

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

		if(errorRsList.size()>1) {
			errorRsList.sort(Comparator
				.comparing(ErrorRs::target) // target 기준으로 정렬
				.thenComparing(ErrorRs::message)); // message 기준으로 정렬
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
		Integer code = GlobalErrorCode.VALIDATION_FAILED.getHttpStatus().value();

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
	public ResponseEntity<RsData<?>> handleGlobalException(GlobalException ex,
		HttpServletRequest request) {

		RsData<?> response = RsData.error(null, ex.getMessage());

		return ResponseEntity
			.status(ex.getErrorCodeIfs().getHttpStatus())
			.body(response);
	}

	@ExceptionHandler(LikesException.class)
	public ResponseEntity<RsData<?>> handleLikesException(LikesException ex) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getStatus())
			.body(response);
	}

	@ExceptionHandler(BookmarkException.class)
	public ResponseEntity<RsData<?>> handleBookmarkException(BookmarkException ex) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getStatus())
			.body(response);
	}

	@ExceptionHandler(FeedException.class)
	public ResponseEntity<RsData<?>> handleFeedException(FeedException ex, HttpServletRequest request) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getErrorCodeIfs().getHttpStatus())
			.body(response);
	}

	@ExceptionHandler(PostException.class)
	public ResponseEntity<RsData<?>> handlePostException(PostException ex) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getHttpStatus())
			.body(response);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<RsData<ErrorRs>> handleUsernameNotFoundException(
		UsernameNotFoundException ex, HttpServletRequest request) {

		log.error("UsernameNotFoundException: {}", ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(RsData.error(ErrorRs.builder()
				.target(request.getRequestURI()) // 에러 발생한 URL
				.code(HttpStatus.NOT_FOUND.value()) // 404 상태 코드
				.message(ex.getMessage()) // 예외 메시지
				.build()));
	}

	@ExceptionHandler(SearchException.class)
	public ResponseEntity<RsData<?>> handleSearchException(SearchException ex, HttpServletRequest request) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getErrorCodeIfs().getHttpStatus())
			.body(response);
	}

	@ExceptionHandler(HashtagException.class)
	public ResponseEntity<RsData<?>> handleHashtagException(HashtagException ex, HttpServletRequest request) {
		RsData<?> response = RsData.error(null, ex.getMessage());
		return ResponseEntity
			.status(ex.getErrorCodeIfs().getHttpStatus())
			.body(response);
	}
}
