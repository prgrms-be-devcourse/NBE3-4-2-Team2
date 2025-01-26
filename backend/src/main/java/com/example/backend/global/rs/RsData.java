package com.example.backend.global.rs;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;

/**
 * @author kwak
 * 공통 응답 구조
 * 정상 응답 케이스 success
 * 에러 케이스 error
 */

public class RsData<T> {
	private final LocalDateTime time;
	private boolean isSuccess;
	private String message;
	private final T data;

	@Builder(access = AccessLevel.PRIVATE)
	private RsData(T data, String message, boolean isSuccess) {
		this.time = LocalDateTime.now();
		this.data = data;
		this.message = message;
		this.isSuccess = isSuccess;
	}

	public static <T> RsData<T> success(T data) {
		return RsData.<T>builder()
			.data(data)
			.isSuccess(true)
			.build();
	}

	public static <T> RsData<T> success(T data, String message) {
		return RsData.<T>builder()
			.data(data)
			.message(message)
			.isSuccess(true)
			.build();
	}

	public static <T> RsData<T> error(T data) {
		return RsData.<T>builder()
			.data(data)
			.isSuccess(false)
			.build();
	}

	public static <T> RsData<T> error(T data, String message) {
		return RsData.<T>builder()
			.data(data)
			.message(message)
			.isSuccess(false)
			.build();
	}
}
