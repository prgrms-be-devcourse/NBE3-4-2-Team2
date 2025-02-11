package com.example.backend.content.search.implement;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import com.example.backend.content.search.dto.SearchPostCursorResponse;
import com.example.backend.content.search.type.SearchType;
import com.example.backend.entity.HashtagEntity;
import com.example.backend.entity.ImageEntity;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostHashtagEntity;
import com.example.backend.global.config.QuerydslConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

/**
 * @author kwak
 * 2025-02-07
 */
@DataJpaTest
@Import({QuerydslConfig.class, SearchFinder.class})
class SearchFinderTest {

	@Autowired
	EntityManager em;
	@Autowired
	JPAQueryFactory queryFactory;
	@Autowired
	SearchFinder searchFinder;

	@BeforeEach
	void setUp() {

		// member1 데이터 및 해시태그 생성
		MemberEntity member1 = persistMember("testUser1", "a1@test.com");
		HashtagEntity catTag = persistHashtag("고양이");

		// member1의 첫 번째 post
		PostEntity member1Post1 = persistPost(member1);
		persistPostHashtag(member1Post1, catTag);
		List.of("url1", "url2").forEach(url -> persistImage(member1Post1, url));

		// member1의 두 번째 post
		PostEntity member1Post2 = persistPost(member1);
		persistPostHashtag(member1Post2, catTag);
		List.of("url3", "url4").forEach(url -> persistImage(member1Post2, url));

		// member2 데이터 및 해시태그 생성
		MemberEntity member2 = persistMember("testUser2", "a2@test.com");
		HashtagEntity dogTag = persistHashtag("강아지");

		// member2의 첫 번째 post
		PostEntity member2Post1 = persistPost(member2);
		persistPostHashtag(member2Post1, dogTag);
		List.of("url5", "url6").forEach(url -> persistImage(member2Post1, url));

		// member2의 두 번째 post
		PostEntity member2Post2 = persistPost(member2);
		persistPostHashtag(member2Post2, dogTag);
		List.of("url7", "url8").forEach(url -> persistImage(member2Post2, url));

		em.flush();
		em.clear();
	}

	private MemberEntity persistMember(String username, String email) {
		MemberEntity member = MemberEntity.builder()
			.username(username)
			.email(email)
			.password("1234")
			.refreshToken(UUID.randomUUID().toString())
			.build();
		em.persist(member);
		return member;
	}

	private PostEntity persistPost(MemberEntity member) {
		PostEntity post = PostEntity.builder()
			.member(member)
			.build();
		em.persist(post);
		return post;
	}

	private void persistImage(PostEntity post, String imageUrl) {
		ImageEntity image = ImageEntity.builder()
			.post(post)
			.imageUrl(imageUrl)
			.build();
		em.persist(image);
	}

	private HashtagEntity persistHashtag(String content) {
		HashtagEntity hashtag = HashtagEntity.builder()
			.content(content)
			.build();
		em.persist(hashtag);
		return hashtag;
	}

	private void persistPostHashtag(PostEntity post, HashtagEntity hashtag) {
		PostHashtagEntity postHashtag = PostHashtagEntity.builder()
			.post(post)
			.hashtag(hashtag)
			.build();
		em.persist(postHashtag);
	}

	@Test
	@DisplayName("작성자 기반 검색 , member1의 post 2개중 최신꺼의 첫번째 이미지만 반환")
	@DirtiesContext
	void test1() {
		// when
		SearchPostCursorResponse response = searchFinder.findByKeyword(
			SearchType.AUTHOR, "testUser1", null, 10);

		// then
		assertThat(response.searchPostResponses()).hasSize(2);
		assertThat(response.searchPostResponses().get(0).imageUrl())
			.isEqualTo("url3");
	}

	@Test
	@DisplayName("작성자 기반 검색, 커서 기반 페이징 정상 작동")
	@DirtiesContext
	void test2() {
		// given
		SearchPostCursorResponse firstPage = searchFinder.findByKeyword(
			SearchType.AUTHOR, "testUser1", null, 1
		);

		Long lastPostId = firstPage.lastPostId();

		// when
		SearchPostCursorResponse nextPage = searchFinder.findByKeyword(
			SearchType.AUTHOR, "testUser1", lastPostId, 10
		);

		// then
		assertThat(nextPage.searchPostResponses()).hasSize(1);  // 두 번째 포스트 하나만 있어야 함
		assertThat(nextPage.searchPostResponses())
			.allMatch(post -> post.postId() < lastPostId);  // ID가 lastPostId 보다 작아야 함
		assertThat(nextPage.hasNext()).isFalse();  // 더 이상 데이터가 없어야 함
		assertThat(lastPostId).isEqualTo(2); // memeber 1의 Post는 두개뿐이기 때문에 lastPostId 2 여야함
	}

	@Test
	@DisplayName("해시태그 기반 검색, 해시태그 관련 post 중 최신꺼의 첫번째 이미지만 반환")
	@DirtiesContext
	void test3() {
		// when
		SearchPostCursorResponse response = searchFinder.findByKeyword(
			SearchType.HASHTAG, "고양이", null, 10);

		// then
		assertThat(response.searchPostResponses()).hasSize(2);  // 고양이 태그 달린 포스트 2개
		assertThat(response.searchPostResponses().get(0).imageUrl())
			.isEqualTo("url3");  // 최신 포스트의 첫번째 이미지
	}

	@Test
	@DisplayName("해시태그 기반 검색, 커서 기반 페이징 정상 작동")
	@DirtiesContext
	void test4() {
		//given
		SearchPostCursorResponse firstPage = searchFinder.findByKeyword(
			SearchType.HASHTAG, "고양이", null, 1
		);

		Long lastPostId = firstPage.lastPostId();

		// when
		SearchPostCursorResponse nextPage = searchFinder.findByKeyword(
			SearchType.HASHTAG, "고양이", lastPostId, 10
		);

		// then
		assertThat(nextPage.searchPostResponses()).hasSize(1);  // 두 번째 포스트 하나만 있어야 함
		assertThat(nextPage.searchPostResponses())
			.allMatch(post -> post.postId() < lastPostId);  // ID가 lastPostId 보다 작아야 함
		assertThat(nextPage.hasNext()).isFalse();  // 더 이상 데이터가 없어야 함
		assertThat(lastPostId).isEqualTo(2); // 고양이태그의 포스트는 두개뿐이기 때문에 lastPostId 2 여야함
	}
}
