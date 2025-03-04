package com.example.backend.social.feed.service;

import static com.example.backend.entity.QMemberEntity.*;
import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.event.CommentEventListener;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.global.event.LikeEventListener;
import com.example.backend.social.feed.dto.FeedListResponse;
import com.example.backend.social.feed.dto.FeedMemberRequest;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.exception.FeedException;
import com.example.backend.social.feed.implement.FeedTestHelper;
import com.querydsl.jpa.impl.JPAQueryFactory;

@SpringBootTest
@DirtiesContext
@Transactional
class FeedServiceTest {

	@Autowired
	private FeedService feedService;

	@Autowired
	private FeedTestHelper feedTestHelper;

	@Autowired
	private JPAQueryFactory queryFactory;

	private MemberEntity member;

	@MockitoBean
	LikeEventListener likeEventListener;
	@MockitoBean
	FollowEventListener followEventListener;
	@MockitoBean
	CommentEventListener commentEventListener;

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();

		member = queryFactory.selectFrom(memberEntity)
			.where(memberEntity.username.eq("user1"))
			.fetchOne();
	}

	@Test
	@DisplayName("메인 피드 validate 테스트")
	void t1() {
		FeedRequest nullTimestamp = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(null)
			.build();

		FeedRequest afterTimestamp = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now().plusDays(1))
			.build();

		FeedRequest overMaxSize = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE + 1)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now().minusDays(1))
			.build();

		Assertions.assertThrows(FeedException.class, () -> {
			feedService.findList(nullTimestamp, 1L);
		});

		Assertions.assertThrows(FeedException.class, () -> {
			feedService.findList(afterTimestamp, 1L);
		});

		Assertions.assertThrows(FeedException.class, () -> {
			feedService.findList(overMaxSize, 1L);
		});
	}

	@Test
	@DisplayName("요청한 피드 개수만큼 받는지 테스트")
	void t2() {
		FeedRequest request = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(0L)
			.timestamp(LocalDateTime.now())
			.build();

		FeedListResponse response = feedService.findList(request, 1L);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(REQUEST_FEED_MAX_SIZE, response.feedList().size());
	}

	@Test
	@DisplayName("멤버 피드 validate 테스트")
	void t3() {
		FeedMemberRequest request = FeedMemberRequest.builder()
			.lastPostId(0L)
			.maxSize(2)
			.build();

		feedService.findMembersList(request, 1L);
	}
}
