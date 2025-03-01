package com.example.backend.content.notification.sse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.content.notification.dto.NotificationResponse;
import com.example.backend.content.notification.type.NotificationType;

/**
 * @author kwak
 * 2025-02-11
 */
@ExtendWith(MockitoExtension.class)
class SseConnectionPoolTest {

	@Mock
	SseConnection connection;
	@InjectMocks
	SseConnectionPool pool;

	@Test
	@DisplayName("add 정상 수행")
	void test1() {
		// given
		String key = "user1";

		// when
		pool.add(key, connection);

		// then
		assertNotNull(pool.get(key));
		assertTrue(pool.get(key).contains(connection));
	}

	@Test
	@DisplayName("remove 정상 수행")
	void test2() {
		// given
		String key = "user1";
		when(connection.getUniqueKey()).thenReturn(key);
		pool.add(key, connection);

		// when
		pool.remove(connection);

		// then
		assertNull(pool.get(key));
	}

	@Test
	@DisplayName("get 정상 수행")
	void test3() {
		// given
		String key = "user1";
		SseConnection connection1 = mock(SseConnection.class);
		SseConnection connection2 = mock(SseConnection.class);

		pool.add(key, connection1);
		pool.add(key, connection2);

		// when
		Set<SseConnection> result = pool.get(key);

		// then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(connection1));
		assertTrue(result.contains(connection2));
	}

	@Test
	@DisplayName("sendNotification 정상 수행")
	void test4() {
		// given
		String key = "1";
		NotificationResponse response = mock(NotificationResponse.class);
		when(response.type()).thenReturn(NotificationType.LIKE);

		SseConnection connection1 = mock(SseConnection.class);
		SseConnection connection2 = mock(SseConnection.class);

		pool.add(key, connection1);
		pool.add(key, connection2);

		// when
		pool.sendNotification(1L, response);

		// then
		verify(connection1, times(1)).sendMessage("LIKE", response);
		verify(connection2, times(1)).sendMessage("LIKE", response);
	}
}
