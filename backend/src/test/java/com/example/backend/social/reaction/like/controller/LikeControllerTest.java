package com.example.backend.social.reaction.like.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.LikeRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.global.event.LikeEventListener;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.user.CustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LikeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberService memberService;

	@Autowired
	private AccessTokenService accessTokenService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private LikeRepository likeRepository;

	@MockitoBean
	private LikeEventListener likeEventListener;

	private String accessToken;
	private MemberEntity testMember;      // 좋아요 주체
	private MemberEntity contentMember;   // 컨텐츠 작성 주체
	private PostEntity testPost;

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
		accessToken = accessTokenService.genAccessToken(testMember);

		// 테스트용 게시물 추가 (contentMember가 작성)
		testPost = PostEntity.builder()
			.content("testContent")
			.member(contentMember)  // contentMember가 작성
			.build();
		testPost = postRepository.save(testPost);

		// SecurityContext 설정
		CustomUser securityUser = new CustomUser(testMember, null);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("1. 좋아요 토글 - 좋아요 적용 성공")
	public void t001() throws Exception {
		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{id}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.param("resourceType", "post")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data.liked").value(true))
			.andExpect(jsonPath("$.data.likeCount").exists());
	}

	@Test
	@DisplayName("2. 좋아요 토글 - 좋아요 취소 성공")
	public void t002() throws Exception {
		// Given - 먼저 좋아요 적용
		mockMvc.perform(post("/api-v1/like/{id}", testPost.getId())
				.header("Authorization", "Bearer " + accessToken)
				.param("resourceType", "post")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		// When - 다시 요청하여 좋아요 취소
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{id}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.param("resourceType", "post")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 취소되었습니다."))
			.andExpect(jsonPath("$.data.liked").value(false))
			.andExpect(jsonPath("$.data.likeCount").exists());
	}

	@Test
	@DisplayName("3. 다양한 리소스 타입에 대한 좋아요 토글 테스트")
	public void t003() throws Exception {
		// 댓글에 대한 좋아요 테스트 (리소스 타입만 변경)
		ResultActions commentLikeResult = mockMvc.perform(post("/api-v1/like/{id}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.param("resourceType", "comment")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		commentLikeResult.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."));

		// 대댓글에 대한 좋아요 테스트
		ResultActions replyLikeResult = mockMvc.perform(post("/api-v1/like/{id}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.param("resourceType", "reply")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		replyLikeResult.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."));
	}

	@Test
	@DisplayName("4. 자신의 게시물에 좋아요 요청 실패 테스트")
	public void t004() throws Exception {
		// Given - testMember가 작성한 게시물 생성
		PostEntity ownPost = PostEntity.builder()
			.content("ownContent")
			.member(testMember)
			.build();
		ownPost = postRepository.save(ownPost);

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/like/{id}", ownPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.param("resourceType", "post")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isConflict())  // 충돌 상태코드 반환 예상
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("자신의 컨텐츠에는 좋아요를 할 수 없습니다."));
	}
}
