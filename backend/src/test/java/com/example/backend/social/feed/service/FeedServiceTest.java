package com.example.backend.social.feed.service;

import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.social.feed.dto.FeedListResponse;
import com.example.backend.social.feed.dto.FeedRequest;
import com.example.backend.social.feed.exception.FeedException;
import com.example.backend.social.feed.implement.FeedTestHelper;

@SpringBootTest
@Transactional
class FeedServiceTest {

	@Autowired
	private FeedService feedService;

	@Autowired
	FeedTestHelper feedTestHelper;

	@BeforeEach
	void setUp() {
		feedTestHelper.setData();
	}

	@Test
	@DisplayName("피드요청 validate 테스트")
	void t1() {
		FeedRequest nullTimestamp = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(null)
			.timestamp(null)
			.build();

		FeedRequest afterTimestamp = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE)
			.lastPostId(null)
			.timestamp(LocalDateTime.now().plusDays(1))
			.build();

		FeedRequest overMaxSize = FeedRequest.builder()
			.maxSize(REQUEST_FEED_MAX_SIZE + 1)
			.lastPostId(null)
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
			.lastPostId(null)
			.timestamp(LocalDateTime.now())
			.build();

		FeedListResponse response = feedService.findList(request, 1L);
		Assertions.assertNotNull(response);
		Assertions.assertEquals(REQUEST_FEED_MAX_SIZE, response.getFeedList().size());
	}

}
