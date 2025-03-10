package com.example.backend.content.notification.sse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author kwak
 * 2025-02-11
 */
@ExtendWith(MockitoExtension.class)
class SseConnectionTest {

	@Mock
	SseConnectionPoolIfs<SseConnection> sseConnectionPoolIfs;
	@Mock
	SseEmitter sseEmitter;
	@Mock
	SseEmitterFactory sseEmitterFactory;

	@Test
	@DisplayName("sseConnection 연결 성공")
	void teat1() {
		// given
		String uniqueKey = "testKey";
		when(sseEmitterFactory.create(any())).thenReturn(sseEmitter);

		// when
		SseConnection connection = SseConnection.connect(uniqueKey, "", sseConnectionPoolIfs);

		// then
		assertNotNull(connection);
		assertEquals(uniqueKey, connection.getUniqueKey());
		verify(sseConnectionPoolIfs, times(1)).add(uniqueKey, connection);
	}

	@Test
	@DisplayName("메시지 전송 성공")
	void test2() throws IOException {
		// given
		String uniqueKey = "testKey";
		String eventName = "testEvent";
		String data = "testData";
		when(sseEmitterFactory.create(any())).thenReturn(sseEmitter);

		SseConnection connection = SseConnection.connect(uniqueKey, "", sseConnectionPoolIfs);

		// when
		connection.sendMessage(eventName, data);

		// then
		verify(sseEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
	}
}
