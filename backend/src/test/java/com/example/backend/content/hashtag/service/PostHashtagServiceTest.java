package com.example.backend.content.hashtag.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@Transactional
class PostHashtagServiceTest {

	@Autowired
	HashtagRepository hashtagRepository;
	@Autowired
	PostHashtagService postHashtagService;
	@Autowired
	HashtagService hashtagService;
	@Autowired
	PostHashtagRepository postHashtagRepository;
	@Autowired
	PostRepository postRepository;
	@Autowired
	MemberRepository memberRepository;

	@Test
	@DisplayName("create 통합 테스트")
	void create() {

		//given
		MemberEntity member = getMemberEntity();
		PostEntity post = getPostEntity(member);
		Set<String> contents = Set.of("고양이", "강아지");

		//todo postHashtagService.create 는 반환값이 필요가 없음
		//when
		// List<PostHashtagEntity> postHashtagEntities = postHashtagService.create(post, contents);
		//
		// //then
		// assertThat(postHashtagEntities)
		// 	.extracting(postHashtag -> postHashtag.getHashtag().getContent())
		// 	.containsExactlyInAnyOrder("고양이", "강아지");
	}

	@Test
	@DisplayName("해시태그 수정 정상 수행 - 기존 해시태그 삭제 및 새로운 해시태그 추가")
	void updatePostHashtags() {

		//given
		MemberEntity member = getMemberEntity();
		PostEntity post = getPostEntity(member);

		HashtagEntity cat = HashtagEntity.builder()
			.content("고양이")
			.build();

		HashtagEntity dog = HashtagEntity.builder()
			.content("강아지")
			.build();

		HashtagEntity pen = HashtagEntity.builder()
			.content("펭귄")
			.build();

		HashtagEntity hashtag1 = hashtagRepository.save(cat);
		HashtagEntity hashtag2 = hashtagRepository.save(dog);

		PostHashtagEntity ph1 = PostHashtagEntity.builder()
			.post(post)
			.hashtag(hashtag1)
			.build();

		PostHashtagEntity ph2 = PostHashtagEntity.builder()
			.post(post)
			.hashtag(hashtag2)
			.build();

		postHashtagRepository.save(ph1);
		postHashtagRepository.save(ph2);

		Set<String> newHashtags = Set.of("강아지", "펭귄");

		//when
		postHashtagService.updatePostHashtags(post, newHashtags);

		//then
		List<PostHashtagEntity> postHashtags = postHashtagRepository.findByPostId(post.getId());
		assertThat(postHashtags)
			.extracting(ph -> ph.getHashtag().getContent())
			.containsExactlyInAnyOrder("강아지", "펭귄");
	}

	private PostEntity getPostEntity(MemberEntity member) {
		return postRepository.save(
			PostEntity.builder()
				.content("#고양이 짱짱귀엽네요")
				.member(member)
				.build());
	}

	private MemberEntity getMemberEntity() {
		return memberRepository.save(MemberEntity.builder()
			.username("testUser")
			.email("testuser@example.com")
			.password("password123")
			.refreshToken(UUID.randomUUID().toString())
			.build());
	}

}
