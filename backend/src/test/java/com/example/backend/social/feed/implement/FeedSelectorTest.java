package com.example.backend.social.feed.implement;

import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.social.feed.Feed;

@SpringBootTest
@Transactional
class FeedSelectorTest {

	@Autowired
	FeedSelector feedSelector;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	FeedTestHelper feedTestHelper;

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();
	}

	@Test
	@DisplayName("피드를 요청하면 팔로우된 유저에 대한 게시글을 얻는다")
	void t1() {
		Optional<MemberEntity> memberOpt = memberRepository.findById(1L);
		Assertions.assertTrue(memberOpt.isPresent());

		MemberEntity member = memberOpt.get();
		Assertions.assertNotEquals(0, member.getFollowingList().size());

		List<Feed> byFollower = feedSelector.findByFollower(member, LocalDateTime.now().plusDays(1), null, 10);
		Assertions.assertNotNull(byFollower);
		Assertions.assertFalse(byFollower.isEmpty());
		Assertions.assertEquals(10, byFollower.size());

		Feed latestFeed = byFollower.get(0);
		Assertions.assertEquals(10L, latestFeed.getPost().getMember().getId());

		Assertions.assertNotNull(latestFeed);
		Assertions.assertNotNull(latestFeed.getPost().getId());
		Assertions.assertEquals(10L, latestFeed.getPost().getMember().getId());
		Assertions.assertEquals(3L, latestFeed.getCommentCount());
		Assertions.assertEquals(5L, latestFeed.getLikeCount());

		Assertions.assertNotNull(latestFeed.getHashTagList());
		Assertions.assertEquals(3, latestFeed.getHashTagList().size());

		Assertions.assertNotNull(latestFeed.getImageUrlList());
		Assertions.assertEquals(2, latestFeed.getImageUrlList().size());
	}

	@Test
	@DisplayName("팔로잉 게시물들은 시간 순으로 내림차 정렬되어 반환된다")
	void t2() {
		Optional<MemberEntity> memberOpt = memberRepository.findById(1L);

		MemberEntity member = memberOpt.get();
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
		Optional<MemberEntity> memberOpt = memberRepository.findById(1L);
		MemberEntity member = memberOpt.get();

		List<Feed> recommendFeedList = feedSelector.findRecommendFinder(member, LocalDateTime.now().plusDays(1),
			LocalDateTime.now().minusDays(1 + RECOMMEND_SEARCH_DATE_RANGE), 10);

		Assertions.assertNotNull(recommendFeedList);
		Assertions.assertEquals(10, recommendFeedList.size());
	}
}
