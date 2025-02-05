package com.example.backend.social.feed.schedular;

import static com.example.backend.entity.QPostHashtagEntity.*;
import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.util.Collections;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.HashtagEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/***
 * FeedScheduler
 * 피드에 관한 스케줄러 로직을 구현하는 클래스
 * @author ChoiHyunSan
 * @since 2025-02-03
 */
@Component
@RequiredArgsConstructor
public class FeedScheduler {

	private final JPAQueryFactory queryFactory;

	@Getter
	private List<HashtagEntity> popularHashtagList = Collections.emptyList();

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional(readOnly = true)
	public void updatePopularHashtag() {
		// 인기 해시태그를 찾기
		List<HashtagEntity> newPopularHashtagList = queryFactory.select(postHashtagEntity.hashtag)
			.from(postHashtagEntity)
			.groupBy(postHashtagEntity.hashtag.id)
			.orderBy(postHashtagEntity.count().desc())
			.limit(POPULAR_HASHTAG_COUNT)
			.fetch();

		popularHashtagList = Collections.unmodifiableList(newPopularHashtagList);
	}
}
