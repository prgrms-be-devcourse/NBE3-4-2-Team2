package com.example.backend.social.feed.schedular;

import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.HashtagRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostHashtagEntity;
import com.example.backend.entity.PostHashtagRepository;
import com.example.backend.entity.PostRepository;

@SpringBootTest
@DirtiesContext
@Transactional
class FeedSchedulerTest {

	@Autowired
	public MemberRepository memberRepository;

	@Autowired
	public PostRepository postRepository;

	@Autowired
	public HashtagRepository hashtagRepository;

	@Autowired
	public PostHashtagRepository postHashtagRepository;

	@Autowired
	public FeedScheduler feedScheduler;

	@BeforeEach
	void setUp() {
		clearRepositories();
		setData();
	}

	@Test
	@DisplayName("가장 많이 사용된 해시태그가 인기 해시태그로 선정된다")
	void t1() {
		feedScheduler.updatePopularHashtag();
		Assertions.assertNotNull(feedScheduler.getPopularHashtagList());

		List<HashtagEntity> popularHashtagList = feedScheduler.getPopularHashtagList();
		Assertions.assertEquals(POPULAR_HASHTAG_COUNT, popularHashtagList.size());

		HashtagEntity hashtag1 = popularHashtagList.get(0);

		Assertions.assertNotNull(hashtag1);
		Assertions.assertEquals("Hashtag1", hashtag1.getContent());

		for (long i = 11; i <= 30; i++) {
			HashtagEntity hashtag = hashtagRepository.findById(i).get();
			Assertions.assertFalse(popularHashtagList.contains(hashtag));
		}
	}

	private void clearRepositories() {
		memberRepository.deleteAll();
		postRepository.deleteAll();
		hashtagRepository.deleteAll();
		postHashtagRepository.deleteAll();
	}

	private void setData() {
		// Member
		MemberEntity member = MemberEntity.builder()
			.username("user1")
			.email("user1@example.com")
			.password("password")
			.build();
		memberRepository.save(member);

		// Post
		List<PostEntity> posts = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			posts.add(PostEntity.builder()
				.member(member)
				.content("Post content" + i)
				.build());
		}
		postRepository.saveAll(posts);

		// Hashtag
		List<HashtagEntity> hashtags = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			hashtags.add(HashtagEntity.builder()
				.content("Hashtag" + (i + 1))
				.build());
		}
		hashtagRepository.saveAll(hashtags);

		// PostHashtag
		// 0 ~ 4 해시태그 -> 1 ~ 3번 게시물
		// 5 ~ 9 해시태그 -> 1 ~ 2번 게시물
		// 10 ~ 29 해시태그 -> 1번 게시물
		List<PostHashtagEntity> postHashtags = new ArrayList<>();
		for (int i = 0; i < 30; i++) {
			if (i < 5) {
				createPostHashtags(postHashtags, posts, hashtags, i, 3);
			} else if (i < 10) {
				createPostHashtags(postHashtags, posts, hashtags, i, 2);
			} else {
				createPostHashtags(postHashtags, posts, hashtags, i, 1);
			}
		}
		postHashtagRepository.saveAll(postHashtags);
	}

	private static void createPostHashtags(List<PostHashtagEntity> postHashtags, List<PostEntity> posts,
		List<HashtagEntity> hashtags, int i, int jValue) {
		for (int j = 0; j < jValue; j++) {
			postHashtags.add(PostHashtagEntity.builder()
				.post(posts.get(j))
				.hashtag(hashtags.get(i))
				.build());
		}
	}
}
