package com.example.backend.social.feed.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.social.feed.Feed;

@SpringBootTest
@Transactional
class FeedSelectorTest {

	private static final Logger log = LoggerFactory.getLogger(FeedSelectorTest.class);
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
		log.info("Name : {}, Id : {}", member.getUsername(), member.getId());
		Assertions.assertNotEquals(0, member.getFollowingList().size());

		List<Feed> byFollower = feedSelector.findByFollower(member, LocalDateTime.now().plusDays(1), null, 10);
		Assertions.assertNotNull(byFollower);
		Assertions.assertFalse(byFollower.isEmpty());
		Assertions.assertEquals(10, byFollower.size());

		Feed latestFeed = byFollower.get(0);
		log.info("Latest feed: {}", latestFeed.getPost().getMember().getId());
		Assertions.assertEquals(20L, latestFeed.getPost().getMember().getId());

		Assertions.assertNotNull(latestFeed);
		Assertions.assertNotNull(latestFeed.getPost().getId());
		Assertions.assertEquals(20L, latestFeed.getPost().getMember().getId());
		Assertions.assertEquals(3L, latestFeed.getCommentCount());
		Assertions.assertEquals(5L, latestFeed.getLikeCount());

		Assertions.assertNotNull(latestFeed.getHashTagList());
		Assertions.assertEquals(3, latestFeed.getHashTagList().size());

		Assertions.assertNotNull(latestFeed.getImageUrlList());
		Assertions.assertEquals(2, latestFeed.getImageUrlList().size());
	}
}
