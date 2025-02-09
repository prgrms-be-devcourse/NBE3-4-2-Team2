package com.example.backend.social.reaction.likes.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.backend.entity.LikesRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.likes.dto.CreateLikeResponse;
import com.example.backend.social.reaction.likes.dto.DeleteLikeResponse;
import com.example.backend.social.reaction.likes.exception.LikesErrorCode;
import com.example.backend.social.reaction.likes.exception.LikesException;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LikesServiceTest {
	@Autowired
	private EntityManager entityManager;

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

		// 시퀀스 초기화 (테스트 데이터 재 생성시 아이디 값이 올라가기 때문)
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE likes ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 멤버 추가
		MemberEntity member = MemberEntity.builder()
			.username("testMember")
			.email("test@gmail.com")
			.password("testPassword")
			.refreshToken(UUID.randomUUID().toString())
			.build();
		testMember = memberRepository.save(member);

		// 테스트용 게시물 추가
		PostEntity post = PostEntity.builder()
			.content("testContent")
			.member(member)
			.build();
		testPost = postRepository.save(post);
	}

	@Test
	@DisplayName("1. 좋아요 적용 테스트")
	public void t001() {
		// Given First
		Long memberId = testMember.getId();
		Long postId = testPost.getId();

		// When First
		CreateLikeResponse createResponse = likesService.createLike(memberId, postId);

		// Then First
		assertNotNull(createResponse);
		assertEquals(memberId, createResponse.memberId());
		assertEquals(postId, createResponse.postId());

		// When Second
		Optional<PostEntity> post = postRepository.findById(postId);

		// Then Second
		assertTrue(post.isPresent());
		assertEquals(1, post.get().getLikeCount());
	}

	@Test
	@DisplayName("2. 좋아요 취소 테스트")
	public void t002() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostId = testPost.getId();

		// When First
		CreateLikeResponse createResponse = likesService.createLike(firstMemberId, firstPostId);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long secondMemberId = createResponse.memberId();
		Long secondPostId = createResponse.postId();

		// When Second
		DeleteLikeResponse deleteResponse = likesService.deleteLike(
			createResponse.likeId(), secondMemberId, secondPostId
		);

		// Then Second
		assertNotNull(deleteResponse);
		assertEquals(firstMemberId, deleteResponse.memberId());
		assertEquals(firstPostId, deleteResponse.postId());

		// When Third
		Optional<PostEntity> post = postRepository.findById(secondPostId);

		// Then Third
		assertTrue(post.isPresent());
		assertEquals(0, post.get().getLikeCount());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버 좋아요 요청 테스트")
	public void t003() {
		// Given
		Long nonExistMemberId = 999L;
		Long postId = testPost.getId();

		// When & Then
		assertThrows(LikesException.class, () -> {
			likesService.createLike(nonExistMemberId, postId);
		}, LikesErrorCode.MEMBER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물 좋아요 요청 테스트")
	public void t004() {
		// Given
		Long memberId = testMember.getId();
		Long nonExistPostId = 999L;

		// When & Then
		assertThrows(LikesException.class, () -> {
			likesService.createLike(memberId, nonExistPostId);
		}, LikesErrorCode.POST_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("5. 좋아요 중복 적용 테스트")
	public void t005() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostId = testPost.getId();

		// When First
		CreateLikeResponse createResponse = likesService.createLike(firstMemberId, firstPostId);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long secondMemberId = createResponse.memberId();
		Long secondPostId = createResponse.postId();

		// When & Then Second
		assertThrows(LikesException.class, () -> {
			likesService.createLike(secondMemberId, secondPostId);
		}, LikesErrorCode.ALREADY_LIKED.getMessage());

		// When Third
		Optional<PostEntity> post = postRepository.findById(secondPostId);

		// Then Third
		assertTrue(post.isPresent());
		assertEquals(1, post.get().getLikeCount());
	}

	@Test
	@DisplayName("6. 적용되지 않은 좋아요 취소 테스트")
	public void t006() {
		// Given
		Long nonExistLikeId = 999L;
		Long memberId = testMember.getId();
		Long postId = testPost.getId();

		// When & Then
		assertThrows(LikesException.class, () -> {
			likesService.deleteLike(nonExistLikeId, memberId, postId);
		}, LikesErrorCode.LIKE_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("7. 좋아요 취소 요청시 다른 유저가 요청하는 테스트")
	public void t007() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostid = testPost.getId();

		// When First
		CreateLikeResponse createResponse = likesService.createLike(firstMemberId, firstPostid);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long likeId = createResponse.likeId();
		Long anotherMemberId = 999L;
		Long secondPostId = createResponse.postId();

		// When & Then Second
		assertThrows(LikesException.class, () -> {
			likesService.deleteLike(likeId, anotherMemberId, secondPostId);
		}, LikesErrorCode.MEMBER_MISMATCH.getMessage());
	}

	@Test
	@DisplayName("8. 다른 게시물 번호의 좋아요 삭제를 요청하는 테스트")
	public void t008() {
		// Given First
		Long firstMemberId = testMember.getId();
		Long firstPostid = testPost.getId();

		// When First
		CreateLikeResponse createResponse = likesService.createLike(firstMemberId, firstPostid);

		// Then First
		assertNotNull(createResponse);

		// Given Second
		Long likeId = createResponse.likeId();
		Long memberId = createResponse.memberId();
		Long anotherPostId = 999L;

		// When & Then Second
		assertThrows(LikesException.class, () -> {
			likesService.deleteLike(likeId, memberId, anotherPostId);
		}, LikesErrorCode.MEMBER_MISMATCH.getMessage());
	}
}
