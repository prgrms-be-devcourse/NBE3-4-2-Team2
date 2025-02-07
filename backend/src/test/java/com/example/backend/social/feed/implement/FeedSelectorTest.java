package com.example.backend.social.feed.implement;

import static com.example.backend.entity.QMemberEntity.*;
import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.social.feed.Feed;
import com.querydsl.jpa.impl.JPAQueryFactory;

@SpringBootTest
@DirtiesContext
@Transactional
class FeedSelectorTest {

	@Autowired
	private FeedSelector feedSelector;

	@Autowired
	private FeedTestHelper feedTestHelper;

	@Autowired
	private JPAQueryFactory queryFactory;

	private MemberEntity member;  // 테스트용 멤버 저장

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();

		member = queryFactory.selectFrom(memberEntity)
			.where(memberEntity.username.eq("user1"))
			.fetchOne();
	}

	@Test
	@DisplayName("피드를 요청하면 팔로우된 유저에 대한 게시글을 얻는다")
	void t1() {
		Assertions.assertNotEquals(0, member.getFollowingList().size());

		List<Feed> byFollower = feedSelector.findByFollower(member, LocalDateTime.now().plusDays(1), null, 10);
		Assertions.assertNotNull(byFollower);
		Assertions.assertFalse(byFollower.isEmpty());
		Assertions.assertEquals(10, byFollower.size());

		Feed latestFeed = byFollower.getFirst();
		Assertions.assertNotNull(latestFeed);
		Assertions.assertNotNull(latestFeed.getPost().getId());
		Assertions.assertEquals(3L, latestFeed.getCommentCount());
		Assertions.assertEquals(5L, latestFeed.getPost().getLikeCount());

		Assertions.assertNotNull(latestFeed.getHashTagList());
		Assertions.assertEquals(3, latestFeed.getHashTagList().size());

		Assertions.assertNotNull(latestFeed.getImageUrlList());
		Assertions.assertEquals(2, latestFeed.getImageUrlList().size());
	}

	@Test
	@DisplayName("팔로잉 게시물들은 시간 순으로 내림차 정렬되어 반환된다")
	void t2() {
		List<Feed> byFollower = feedSelector.findByFollower(member, LocalDateTime.now().plusDays(1), null, 10);

		for (int i = 0; i < byFollower.size() - 1; i++) {
			LocalDateTime front = byFollower.get(i).getPost().getCreateDate();
			LocalDateTime back = byFollower.get(i + 1).getPost().getCreateDate();
			Assertions.assertFalse(front.isBefore(back));
		}
	}

	@Test
	@DisplayName("추천 게시물 요청")
	void t3() {
		List<Feed> recommendFeedList = feedSelector.findRecommendFinder(member, LocalDateTime.now().plusDays(1),
			LocalDateTime.now().minusDays(1 + RECOMMEND_SEARCH_DATE_RANGE), 10);

		Assertions.assertNotNull(recommendFeedList);
		Assertions.assertEquals(10, recommendFeedList.size());
	}
}
