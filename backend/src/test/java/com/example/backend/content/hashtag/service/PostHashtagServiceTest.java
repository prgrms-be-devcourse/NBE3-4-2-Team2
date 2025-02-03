package com.example.backend.content.hashtag.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

		MemberEntity member = memberRepository.save(MemberEntity.builder()
			.username("testUser")
			.email("testuser@example.com")
			.password("password123")
			.build());

		PostEntity post = postRepository.save(
			PostEntity.builder()
				.content("#고양이 짱짱귀엽네요")
				.member(member)
				.build());

		Set<String> contents = Set.of("고양이", "강아지");

		List<PostHashtagEntity> postHashtagEntities = postHashtagService.create(post, contents);
		assertThat(postHashtagEntities.size()).isEqualTo(2);
		assertThat(postHashtagEntities.get(0).getHashtag().getContent()).isEqualTo("고양이");
		assertThat(postHashtagEntities.get(1).getHashtag().getContent()).isEqualTo("강아지");

	}
}
