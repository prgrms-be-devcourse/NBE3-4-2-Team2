package com.example.backend.content.notification.sse;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 각각의 단일 사용자 연결을 담당하는 객체
 * @author kwak
 * 2025-02-09
 */
@Getter
public class SseConnection {

	private final String uniqueKey;
	private final SseEmitter sseEmitter;
	private final SseEmitterFactory sseEmitterFactory;
	private final SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs;

	private static final Long DEFAULT_MINUTE = 1000L * 60 * 5;

	private SseConnection(
		String uniqueKey,
		SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs,
		SseEmitterFactory sseEmitterFactory
	) {
		this.uniqueKey = uniqueKey;
		this.sseEmitterFactory = sseEmitterFactory;
		this.sseEmitter = sseEmitterFactory.create(DEFAULT_MINUTE);
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
		String uniqueKey,
		SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs,
		SseEmitterFactory sseEmitterFactory
	) {
		SseConnection connection = new SseConnection(uniqueKey, sseConnectionPoolIfs, sseEmitterFactory);
		sseConnectionPoolIfs.add(uniqueKey, connection);
		return connection;
	}

	public void sendMessage(String eventName, Object data) {
		try {
			SseEmitter.SseEventBuilder event = SseEmitter.event()
				.name(eventName) // 이벤트 이름 설정
				.data(data); // 전송할 데이터 설정
			this.sseEmitter.send(event);

		} catch (IOException e) {
			sseEmitter.completeWithError(e);
		}
	}
}
