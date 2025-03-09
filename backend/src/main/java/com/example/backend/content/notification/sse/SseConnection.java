package com.example.backend.content.notification.sse;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 각각의 단일 사용자 연결을 담당하는 객체
 * @author kwak
 * 2025-02-09
 */
@Getter
@Slf4j
public class SseConnection {

	private final String uniqueKey;
	private final String browserName;
	private final SseEmitter sseEmitter;
	private final SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs;

	private static final Long DEFAULT_MINUTE = 1000L * 30;

	private SseConnection(
		String uniqueKey,
		String browserName,
		SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs
	) {
		this.uniqueKey = uniqueKey;
		this.browserName = browserName;
		this.sseEmitter = new SseEmitter(DEFAULT_MINUTE);
		this.sseConnectionPoolIfs = sseConnectionPoolIfs;

		this.sseEmitter.onTimeout(sseEmitter::complete);
		this.sseEmitter.onCompletion(() -> sseConnectionPoolIfs.remove(this));
		this.sseEmitter.onError(ex -> sseConnectionPoolIfs.remove(this));
	}

	/**
	 * connect() 시 sseConnection 객체 생성하고 Pool 에 add 까지 완료
	 * @author kwak
	 * @since 2025-02-10
	 */
	public static SseConnection connect(
		String userId,
		String browserName,
		SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs
	) {
		SseConnection connection = new SseConnection(userId, browserName, sseConnectionPoolIfs);
		sseConnectionPoolIfs.add(userId, browserName, connection);
		return connection;
	}

	public void sendMessage(String eventName, Object data) {
		try {

			SseEmitter.SseEventBuilder event = SseEmitter.event()
				.name(eventName) // 이벤트 이름 설정
				.data(data); // 전송할 데이터 설정
			this.sseEmitter.send(event);
			log.info("Message sent successfully: {}", eventName);

		} catch (Exception e) {
			log.error("Message sent failed: {}", eventName);
			sseEmitter.completeWithError(e);
		}
	}
}
