package com.example.backend.global.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.content.hashtag.service.HashtagService;
import com.example.backend.content.hashtag.service.HashtagUsageCollector;
import com.example.backend.content.hashtag.service.PostHashtagService;
import com.example.backend.entity.HashtagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 해시태그 관련 스케줄러 업무를 실행
 * @author kwak
 * 2025/02/03
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HashtagScheduler {

	private final HashtagUsageCollector hashtagUsageCollector;
	private final HashtagRepository hashtagRepository;
	private final PostHashtagService postHashtagService;
	private final HashtagService hashtagService;

	/**
	 * 10분마다 한번씩 사용된 hashtag 들을 모아 최근사용시간을 수정하는 쿼리를 발생
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Transactional
	@Scheduled(fixedRate = 6000 * 10)
	public void updateHashtagUsage() {
		Set<Long> hashtagUsageData = hashtagUsageCollector.flushUsageStorage();
		if (hashtagUsageData.isEmpty()) {
			log.info("Not Exist hashtag Usage Data");
			return;
		}
		hashtagRepository.bulkLastUsedAt(hashtagUsageData, LocalDateTime.now());
	}

	/**
	 * 매일 오전 6시 3개월 이상 사용하지 않은 해시태그 삭제 진행
	 * 오래된 hashtag 들 조회해서 없으면 log 남기고 return
	 * 존재하면 posthashtag 부터 삭제하고 hashtag 삭제
	 * @author kwak
	 * @since 2025-02-03
	 */
	@Transactional
	@Scheduled(cron = "0 0 6 * * *")
	public void deleteOldHashtag() {
		List<Long> oldHashtagIds = hashtagService.findOldHashtags();
		if (oldHashtagIds.isEmpty()) {
			log.info("Not Exist Old Hashtags");
			return;
		}
		postHashtagService.deleteByHashtagIds(oldHashtagIds);
		hashtagService.deleteOldHashtag(oldHashtagIds);
	}
}
