package com.example.backend.social.reaction.bookmark.exception;

import org.springframework.http.HttpStatus;

/**
 * 북마크 예외 처리 클래스
 * 북마크에서 발생하는 예외를 처리하는 클래스
 *
 * @author Metronon
 * @since 2025-01-31
 */
public class BookmarkException extends RuntimeException {
	private final BookmarkErrorCode bookmarkErrorCode;

	public BookmarkException(BookmarkErrorCode bookmarkErrorCode) {
		super(bookmarkErrorCode.getMessage());
		this.bookmarkErrorCode = bookmarkErrorCode;
	}

	public HttpStatus getStatus() {
		return bookmarkErrorCode.httpStatus;
	}
}
