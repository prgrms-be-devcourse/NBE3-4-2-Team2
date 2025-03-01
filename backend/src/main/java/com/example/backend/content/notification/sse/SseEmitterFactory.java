package com.example.backend.content.notification.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author kwak
 * 2025-02-11
 */
@Component
public class SseEmitterFactory {

	public SseEmitter create(Long timeout) {
		return new SseEmitter(timeout);
	}
}
