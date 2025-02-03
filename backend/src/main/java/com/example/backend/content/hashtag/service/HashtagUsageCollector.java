package com.example.backend.content.hashtag.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 사용된 hashtag 의 데이터를 수집하고 처리
 * @author kwak
 * 2025/02/03
 */
@Component
public class HashtagUsageCollector {

	private final Set<Long> usageStorage = ConcurrentHashMap.newKeySet();

	public void addUsageStorage(Long hashtagId) {
		usageStorage.add(hashtagId);
	}

	public Set<Long> flushUsageStorage() {
		Set<Long> copy = new HashSet<>(usageStorage);
		usageStorage.clear();
		return copy;
	}

}
