package com.example.backend.content.notification.sse;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.backend.content.notification.dto.NotificationLikeResponse;
import com.example.backend.content.notification.exception.NotificationErrorCode;
import com.example.backend.content.notification.exception.NotificationException;

/**
 * 모든 사용자의 연결을 관리하는 관리자 기능
 * @author kwak
 * 2025-02-09
 */
@Component
public class SseConnectionPool implements SseConnectionPoolIfs<SseConnection> {

	// 한 사용자가 다른 브라우저로 접근할 시 여러 연결을 동시에 가지기 위해 Set
	private final Map<String, Set<SseConnection>> connectionPool = new ConcurrentHashMap<>();

	// key 가 존재하면 기존 Set 에 connection 저장
	// key 가 존재하지 않으면 새로운 Set 생성해서 connection 저장
	@Override
	public void add(String key, SseConnection connection) {
		connectionPool.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(connection);
	}

	@Override
	public Set<SseConnection> get(String key) {
		return connectionPool.get(key);
	}

	@Override
	public void remove(SseConnection session) {
		Set<SseConnection> connections = connectionPool.get(session.getUniqueKey());
		// connection 이 있으면 해당 connection 을 제거 , 없으면 key 로 제거
		if (connections != null) {
			connections.remove(session);
			if (connections.isEmpty()) {
				connectionPool.remove(session.getUniqueKey());
			}
		}
	}
	// 연결이 여러 곳에서 되어 있을 경우 연결마다 알림을 전송 처리
	public void sendNotification(Long userId, NotificationLikeResponse response) {
		Set<SseConnection> connections = get(userId.toString());

		if (connections == null || connections.isEmpty()) {
			throw new NotificationException(NotificationErrorCode.NO_ACTIVE_CONNECTION);
		}
		connections.forEach(sseConnection ->
			sseConnection.sendMessage(response.type().toString(), response));
	}
}
