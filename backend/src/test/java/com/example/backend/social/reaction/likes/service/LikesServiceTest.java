package com.example.backend.social.reaction.likes.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.backend.entity.LikesRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.likes.dto.LikesResponse;
import com.example.backend.social.reaction.likes.exception.LikesErrorCode;
import com.example.backend.social.reaction.likes.exception.LikesException;

@SpringBootTest
@AutoConfigureMockMvc
public class LikesServiceTest {

	@Autowired
	private LikesService likesService;

	@Autowired
	private LikesRepository likesRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	private MemberEntity testMember;
	private PostEntity testPost;

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		likesRepository.deleteAll();
		postRepository.deleteAll();
		memberRepository.deleteAll();

		// 테스트용 멤버 추가
		MemberEntity member = MemberEntity.builder()
			.username("testMember")
			.email("test@gmail.com")
			.password("testPassword")
			.build();
		testMember = memberRepository.save(member);

		// 테스트용 포스트 추가
		PostEntity post = PostEntity.builder()
			.content("testContent")
			.member(member)
			.build();
		testPost = postRepository.save(post);
	}

	@Test
	@DisplayName("1. 좋아요 적용 테스트")
	public void t001() {
		LikesResponse response = likesService.createLike(testMember.getId(), testPost.getId());
		assertNotNull(response);
		assertEquals(testMember.getId(), response.getMemberId());
		assertEquals(testPost.getId(), response.getPostId());
	}

	@Test
	@DisplayName("2. 좋아요 취소 테스트")
	public void t002() {
		LikesResponse response = likesService.createLike(testMember.getId(), testPost.getId());
		assertNotNull(response);

		LikesResponse deleteResponse = likesService.deleteLike(testMember.getId(), testPost.getId());
		assertNotNull(deleteResponse);
		assertEquals(testMember.getId(), deleteResponse.getMemberId());
		assertEquals(testPost.getId(), deleteResponse.getPostId());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버 좋아요 테스트")
	public void t003() {
		assertThrows(LikesException.class, () -> {
			likesService.createLike(99L, testPost.getId());
		}, LikesErrorCode.MEMBER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물 좋아요 테스트")
	public void t004() {
		assertThrows(LikesException.class, () -> {
			likesService.createLike(testMember.getId(), 99L);
		}, LikesErrorCode.POST_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("5. 좋아요 중복 적용 테스트")
	public void t005() {
		LikesResponse response = likesService.createLike(testMember.getId(), testPost.getId());
		assertNotNull(response);

		assertThrows(LikesException.class, () -> {
			likesService.createLike(testMember.getId(), testPost.getId());
		}, LikesErrorCode.ALREADY_LIKED.getMessage());
	}

	@Test
	@DisplayName("6. 좋아요가 없는 게시물에 좋아요 취소 테스트")
	public void t006() {
		assertThrows(LikesException.class, () -> {
			likesService.deleteLike(testMember.getId(), testPost.getId());
		}, LikesErrorCode.LIKE_NOT_FOUND.getMessage());
	}

}


