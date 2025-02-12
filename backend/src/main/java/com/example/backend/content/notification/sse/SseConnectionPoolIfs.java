package com.example.backend.content.notification.sse;

import java.util.Set;

/**
 * @author kwak
 * 2025-02-09
 */
public interface SseConnectionPoolIfs<R> {
	// Connection Pool 에 세션 추가
	void add(String key, R session);

	// Pool 에서 Session 꺼내기
	Set<R> get(String key);

	// Connection Pool 에서 세션 제거
	void remove(R session);
}
