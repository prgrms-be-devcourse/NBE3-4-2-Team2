package com.example.backend.global;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Builder;

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
}