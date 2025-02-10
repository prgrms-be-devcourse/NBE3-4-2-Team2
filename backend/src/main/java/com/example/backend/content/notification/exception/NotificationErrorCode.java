package com.example.backend.content.notification.exception;

import org.springframework.http.HttpStatus;

import com.example.backend.global.error.ErrorCodeIfs;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author kwak
 * 2025/02/10
 */
@AllArgsConstructor
@Getter
public enum NotificationErrorCode implements ErrorCodeIfs {

	FAILED_SEND(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패하였습니다."),
	NO_ACTIVE_CONNECTION(HttpStatus.INTERNAL_SERVER_ERROR, "활성화된 연결이 없습니다."),
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String description;

}
