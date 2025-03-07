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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.CommentEventListener;
import com.example.backend.global.event.FollowEventListener;
import com.example.backend.global.event.LikeEventListener;
import com.example.backend.social.feed.Feed;
import com.example.backend.social.reaction.like.service.LikeSyncService;
import com.querydsl.jpa.impl.JPAQueryFactory;

@SpringBootTest
@DirtiesContext
@Transactional
class FeedSelectorTest {

	@Autowired
	private FeedSelectorCache feedSelector;

	@Autowired
	private FeedTestHelper feedTestHelper;

	@Autowired
	private JPAQueryFactory queryFactory;

	private MemberEntity member;  // 테스트용 멤버 저장

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private LikeSyncService likeSyncService;

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
	@DisplayName("피드를 요청하면 팔로우된 유저에 대한 게시글을 얻는다")
	void t1() {
		likeSyncService.syncToDatabase();

		Assertions.assertNotEquals(0, member.getFollowingList().size());

		List<Feed> byFollower = feedSelector.findByFollower(member, 0L, 10);
		Assertions.assertNotNull(byFollower);
		Assertions.assertFalse(byFollower.isEmpty());
		Assertions.assertEquals(10, byFollower.size());

		Feed latestFeed = byFollower.getFirst();
		Assertions.assertNotNull(latestFeed);
		Assertions.assertNotNull(latestFeed.getPost().getId());
		Assertions.assertEquals(3L, latestFeed.getCommentCount());
		Assertions.assertEquals(0L, latestFeed.getPost().getLikeCount());

		Assertions.assertNotNull(latestFeed.getHashTagList());
		Assertions.assertEquals(3, latestFeed.getHashTagList().size());

		Assertions.assertNotNull(latestFeed.getImageUrlList());
		Assertions.assertEquals(2, latestFeed.getImageUrlList().size());
	}

	@Test
	@DisplayName("팔로잉 게시물들은 시간 순으로 내림차 정렬되어 반환된다")
	void t2() {
		List<Feed> byFollower = feedSelector.findByFollower(member, 0L, 10);

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

	@Test
	@DisplayName("단건 조회 테스트")
	void t4() {

		PostEntity post = PostEntity.builder()
			.member(member)
			.isDeleted(false)
			.content("content")
			.build();

		postRepository.save(post);

		Feed byPostId = feedSelector.findByPostId(post.getId(), member);
		Assertions.assertNotNull(byPostId);
		Assertions.assertEquals("content", byPostId.getPost().getContent());
		Assertions.assertEquals(0, byPostId.getPost().getLikeCount());
		Assertions.assertEquals(0, byPostId.getCommentCount());
		Assertions.assertEquals(0, byPostId.getImageUrlList().size());
		Assertions.assertEquals(0, byPostId.getHashTagList().size());
	}

	@Test
	@DisplayName("멤버 게시물 조회 테스트")
	void t5() {
		List<Feed> byMember1 = feedSelector.findByMember(member, 0L, 2);
		Assertions.assertNotNull(byMember1);
		Assertions.assertEquals(2, byMember1.size());
		Long lastPostId = byMember1.getLast().getPost().getId();

		List<Feed> byMember2 = feedSelector.findByMember(member, lastPostId, 2);
		Assertions.assertNotNull(byMember2);
		Assertions.assertEquals(2, byMember2.size());

		Assertions.assertTrue(byMember2.getFirst().getPost().getId() < lastPostId);
	}
}
