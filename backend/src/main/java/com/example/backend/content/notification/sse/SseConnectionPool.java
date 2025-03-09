package com.example.backend.content.notification.sse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.backend.content.notification.dto.NotificationResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 모든 사용자의 연결을 관리하는 관리자 기능
 * 한 사용자는 각 브라우저당 sse 연결 하나만 허용
 * @author kwak
 * 2025-02-09
 */
@Component
@Slf4j
public class SseConnectionPool implements SseConnectionPoolIfs<SseConnection> {

	private final Map<String, Map<String, SseConnection>> connectionPool = new ConcurrentHashMap<>();

	// key 가 존재하면 기존의 sse connection 을 닫아주고 새로 생성
	// key 가 존재하지 않으면 새 connection 생성
	@Override
	public void add(String key, String browserName, SseConnection connection) {
		Map<String, SseConnection> browserConnections = connectionPool.computeIfAbsent(key,
			k -> new ConcurrentHashMap<>());

		// 기존에 존재하는 key 면 oldConnection 반환
		SseConnection oldConnection = browserConnections.put(browserName, connection);
		log.info("new connection ,old connection : {} {}", connection, oldConnection);

		if (oldConnection != null) {
			try {
				oldConnection.getSseEmitter().complete();
				log.info("old connection complete");
			} catch (Exception e) {
				log.debug("Error closing previous connection for user {} browser {}", key, browserName, e);
			}
		}
	}

	@Override
	public Map<String, SseConnection> get(String key) {
		return connectionPool.get(key);
	}

	@Override
	public void remove(SseConnection session) {
		Map<String, SseConnection> browserConnections = connectionPool.get(session.getUniqueKey());
		// connection 이 있으면 해당 connection 을 제거 , 없으면 key 로 제거
		if (browserConnections != null) {
			browserConnections.remove(session.getBrowserName());
			if (browserConnections.isEmpty()) {
				connectionPool.remove(session.getUniqueKey());
			}
		}
	}

	// 연결이 여러 곳에서 되어 있을 경우 연결마다 알림을 전송 처리
	public void sendNotification(Long userId, NotificationResponse response) {
		Map<String, SseConnection> browserConnections = get(userId.toString());

		if (browserConnections == null || browserConnections.isEmpty()) {
			log.info("No active connection for user: {}", userId);
			return;
			// todo 추후 상대방이 접속하지 않았을 시 알림 목록으로 보냄
		}
		browserConnections.forEach((browserName, sseConnection) ->
			sseConnection.sendMessage("message", response));

	}

	/*@Scheduled(fixedRate = 20000)
	public void sendHeartbeat() {
		connectionPool.forEach((key, connections) -> {
			connections.forEach((browserName, connection) -> {
				try {
					connection.sendMessage("heartbeat", "thump");
					log.info("Sending heartbeat for user {} browser {} connection {}", key, browserName, connection);
				} catch (Exception e) {
					log.info("Falid to send heartbeat = {}", key, e);
					remove(connection);
				}
			});
		});
	}*/

}
