package com.example.backend.global.scheduler;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.hashtag.service.HashtagUsageCollector;
import com.example.backend.entity.HashtagRepository;

import lombok.RequiredArgsConstructor;

/**
 * 해시태그 관련 스케줄러 업무를 실행
 * @author kwak
 * 2025/02/03
 */
@Component
@RequiredArgsConstructor
public class HashtagScheduler {

	private final HashtagUsageCollector hashtagUsageCollector;
	private final HashtagRepository hashtagRepository;

	/**
	 * 10분마다 한번씩 사용된 hashtag 들을 모아 최근사용시간을 수정하는 쿼리를 발생
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Transactional
	@Scheduled(fixedRate = 6000 * 10)
	public void updateHashtagUsage() {
		Set<Long> hashtagUsageData = hashtagUsageCollector.flushUsageStorage();
		if (!hashtagUsageData.isEmpty()) {
			hashtagRepository.bulkLastUsedAt(hashtagUsageData, LocalDateTime.now());
		}
	}
}
