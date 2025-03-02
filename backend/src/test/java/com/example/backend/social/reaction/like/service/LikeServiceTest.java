package com.example.backend.social.reaction.like.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.LikeRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.LikeEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.social.exception.SocialException;
import com.example.backend.social.reaction.like.dto.LikeToggleResponse;
import com.example.backend.social.reaction.like.scheduler.LikeSyncManager;
import com.example.backend.social.reaction.like.util.RedisKeyUtil;
import com.example.backend.social.reaction.like.util.component.RedisLikeService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LikeServiceTest {
	@Autowired
	private EntityManager entityManager;

	@Autowired
	private LikeService likeService;

	@Autowired
	private RedisLikeService redisLikeService;

	@Autowired
	private LikeSyncManager likeSyncManager;

	@Autowired
	private LikeRepository likeRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	private MemberEntity testMember;      // 좋아요 주체
	private MemberEntity contentMember;   // 컨텐츠 작성 주체
	private PostEntity testPost;

	@Autowired
	private MemberService memberService;

	@MockitoBean
	LikeEventListener likeEventListener;

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		likeRepository.deleteAll();
		postRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE like ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 멤버 추가 (testMember는 좋아요를 누르는 주체)
		testMember = memberService.join("testMember", "testPassword", "test@gmail.com");
		// contentMember는 컨텐츠를 작성하는 주체
		contentMember = memberService.join("contentMember", "testPassword", "content@gmail.com");

		// 테스트용 게시물 추가 (contentMember가 작성)
		PostEntity post = PostEntity.builder()
			.content("testContent")
			.member(contentMember)  // 컨텐츠 작성자는 contentMember
			.build();
		testPost = postRepository.save(post);
	}

	@Test
	@DisplayName("1. 좋아요 토글 - 좋아요 적용 테스트")
	public void toggleLikeToLiked() {
		// Given
		long memberId = testMember.getId();  // 좋아요 주체는 testMember
		String resourceType = "post";
		Long resourceId = testPost.getId();

		// When
		LikeToggleResponse response = likeService.toggleLike(memberId, resourceType, resourceId);

		// Then
		assertNotNull(response);
		assertTrue(response.isLiked());
		assertEquals(1L, response.likeCount());

		// Redis 상태 확인
		String countKey = RedisKeyUtil.getLikeCountKey("POST", resourceId);
		Long redisCount = redisLikeService.getLikeCount(countKey);
		assertEquals(1L, redisCount);
	}

	@Test
	@DisplayName("2. 좋아요 토글 - 좋아요 취소 테스트")
	public void toggleLikeToUnliked() {
		// Given - 먼저 좋아요 적용
		long memberId = testMember.getId();  // 좋아요 주체는 testMember
		String resourceType = "post";
		Long resourceId = testPost.getId();
		likeService.toggleLike(memberId, resourceType, resourceId);

		// When - 다시 토글하여 좋아요 취소
		LikeToggleResponse response = likeService.toggleLike(memberId, resourceType, resourceId);

		// Then
		assertNotNull(response);
		assertFalse(response.isLiked());
		assertEquals(0L, response.likeCount());

		// Redis 상태 확인
		String countKey = RedisKeyUtil.getLikeCountKey("POST", resourceId);
		Long redisCount = redisLikeService.getLikeCount(countKey);
		assertEquals(0L, redisCount);
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버가 좋아요 요청 테스트")
	public void nonExistentMemberToggleLike() {
		// Given
		long nonExistMemberId = 999L;
		String resourceType = "post";
		Long resourceId = testPost.getId();

		// When & Then
		assertThrows(SocialException.class, () -> {
			likeService.toggleLike(nonExistMemberId, resourceType, resourceId);
		});
	}

	@Test
	@DisplayName("4. 존재하지 않는 리소스에 좋아요 요청 테스트")
	public void nonExistentResourceToggleLike() {
		// Given
		long memberId = testMember.getId();
		String resourceType = "post";
		Long nonExistResourceId = 999L;

		// When & Then
		assertThrows(SocialException.class, () -> {
			likeService.toggleLike(memberId, resourceType, nonExistResourceId);
		});
	}

	@Test
	@DisplayName("5. 자신의 컨텐츠에 좋아요 요청 테스트")
	public void toggleLikeOnOwnContent() {
		// Given - testMember가 작성한 게시물 생성
		PostEntity myPost = PostEntity.builder()
			.content("myContent")
			.member(testMember)  // 본인이 작성한 게시물
			.build();
		myPost = postRepository.save(myPost);

		long memberId = testMember.getId();
		String resourceType = "post";
		Long resourceId = myPost.getId();

		// When & Then
		assertThrows(SocialException.class, () -> {
			likeService.toggleLike(memberId, resourceType, resourceId);
		}, "자신의 컨텐츠에는 좋아요를 할 수 없습니다.");
	}

	@Test
	@DisplayName("6. 다양한 리소스 타입에 대한 좋아요 토글 테스트")
	public void toggleLikeForDifferentResourceTypes() {
		// Given
		long memberId = testMember.getId();
		Long resourceId = testPost.getId();

		// When - 댓글에 좋아요
		LikeToggleResponse commentResponse = likeService.toggleLike(memberId, "comment", resourceId);

		// Then
		assertTrue(commentResponse.isLiked());
		assertEquals(1L, commentResponse.likeCount());

		// When - 대댓글에 좋아요
		LikeToggleResponse replyResponse = likeService.toggleLike(memberId, "reply", resourceId);

		// Then
		assertTrue(replyResponse.isLiked());
		assertEquals(1L, replyResponse.likeCount());
	}
}
