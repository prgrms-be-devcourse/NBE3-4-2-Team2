package com.example.backend.social.feed.schedular;

import static com.example.backend.entity.QPostHashtagEntity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
	private List<Long> favoriteHashtagList = new ArrayList<>();

	@Scheduled(cron = "0 0 0 * * *")
	public void dailyTask() {
		// 인기 해시태그를 찾기
		List<Long> newFavoriteHashtagList = queryFactory.select(postHashtagEntity.hashtag.id)
			.from(postHashtagEntity)
			.groupBy(postHashtagEntity.hashtag.id)
			.orderBy(postHashtagEntity.count().desc())
			.limit(10)
			.fetch();

		favoriteHashtagList = Collections.unmodifiableList(newFavoriteHashtagList);
	}
}
