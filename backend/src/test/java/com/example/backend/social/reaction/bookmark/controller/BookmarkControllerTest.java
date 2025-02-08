package com.example.backend.social.reaction.bookmark.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.user.SecurityUser;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookmarkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	private String accessToken;
	private MemberEntity testMember;
	private PostEntity testPost;

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		bookmarkRepository.deleteAll();
		postRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE bookmark ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 테스트용 멤버 추가
		testMember = memberService.join("testMember", "testPassword", "test@gmail.com");
		accessToken = memberService.genAccessToken(testMember);

		// 테스트용 게시물 추가
		testPost = PostEntity.builder()
			.content("testContent")
			.member(testMember)
			.build();
		testPost = postRepository.save(testPost);

		// SecurityContext 설정
		SecurityUser securityUser = new SecurityUser(testMember.getId(), testMember.getUsername(), testMember.getPassword(), new ArrayList<>());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	@DisplayName("1. 북마크 등록 테스트")
	public void t001() throws Exception {
		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 추가되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("2. 북마크 삭제 테스트")
	public void t002() throws Exception {
		// When & Then First
		MvcResult bookmarkResult = mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		// Given Second
		String bookmarkResponse = bookmarkResult.getResponse().getContentAsString();
		JsonNode bookmarkRoot = objectMapper.readTree(bookmarkResponse);
		Long bookmarkId = bookmarkRoot.path("data").path("id").asLong();

		DeleteBookmarkRequest deleteRequest = DeleteBookmarkRequest.builder()
			.id(bookmarkId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/bookmark/{postId}", testPost.getId())
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 제거되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버가 북마크 등록 테스트")
	public void t003() throws Exception {
		// Given
		Long nonExistentMemberId = 99L;
		SecurityUser securityUser = new SecurityUser(nonExistentMemberId, "nonExistentUser", "password", new ArrayList<>());
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("멤버 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물의 북마크 삭제 테스트")
	public void t004() throws Exception {
		// Given
		Long nonExistentPostId = 99L;

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/bookmark/{postId}", nonExistentPostId)
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("게시물 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("5. 이미 북마크로 등록된 게시물 중복 등록 테스트")
	public void t005() throws Exception {
		// Given
		mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// When
		ResultActions resultActions = mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 등록된 북마크 입니다."));
	}

	@Test
	@DisplayName("6. 북마크에 없는 게시물 북마크 삭제 테스트")
	public void t006() throws Exception {
		// Given
		DeleteBookmarkRequest deleteRequest = DeleteBookmarkRequest.builder()
			.id(1L)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/bookmark/{postId}", testPost.getId())
			.header("Authorization", "Bearer " + accessToken)
			.content(deleteRequestJson)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("북마크 정보를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("7. 북마크 삭제시 다른 유저가 요청하는 테스트")
	public void t007() throws Exception {
		// Given First
		SecurityUser testSecurityUser = new SecurityUser(testMember.getId(), testMember.getUsername(), testMember.getPassword(), new ArrayList<>());

		// When First
		ResultActions resultActions = mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
			.with(user(testSecurityUser))
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then First
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// Given Second
		// 북마크 정보 추출
		String bookmarkResponse = resultActions.andReturn().getResponse().getContentAsString();
		JsonNode bookmarkRoot = objectMapper.readTree(bookmarkResponse);
		Long bookmarkId = bookmarkRoot.path("data").path("id").asLong();

		// 새로운 멤버 추가 및 토큰 발급
		MemberEntity otherMember = memberService.join("otherMember", "otherPassword", "other@gmail.com");
		String otherAccessToken = memberService.genAccessToken(otherMember);

		SecurityUser otherSecurityUser = new SecurityUser(otherMember.getId(), otherMember.getUsername(), otherMember.getPassword(), new ArrayList<>());

		// Request DTO 정보 빌드
		DeleteBookmarkRequest deleteRequest = DeleteBookmarkRequest.builder()
			.id(bookmarkId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions2 = mockMvc.perform(delete("/api-v1/bookmark/{postId}", testPost.getId())
			.with(user(otherSecurityUser))
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + otherAccessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions2.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("북마크에 접근할 권한이 없습니다."));
	}

	@Test
	@DisplayName("8. DB에 등록된 북마크의 게시물과 해당 게시물이 다른 경우의 테스트")
	public void t008() throws Exception {
		// Given First
		SecurityUser testSecurityUser = new SecurityUser(testMember.getId(), testMember.getUsername(), testMember.getPassword(), new ArrayList<>());

		// When & Then First
		ResultActions bookmarkResult = mockMvc.perform(post("/api-v1/bookmark/{postId}", testPost.getId())
				.with(user(testSecurityUser))
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		// Given Second
		// 북마크 ID 추출
		String bookmarkResponse = bookmarkResult.andReturn().getResponse().getContentAsString();
		JsonNode bookmarkRoot = objectMapper.readTree(bookmarkResponse);
		Long bookmarkId = bookmarkRoot.path("data").path("id").asLong();

		// 새로운 게시물 생성
		PostEntity otherPost = PostEntity.builder()
			.content("otherContent")
			.member(testMember)
			.build();
		otherPost = postRepository.save(otherPost);

		// Request DTO 정보 빌드
		DeleteBookmarkRequest deleteRequest = DeleteBookmarkRequest.builder()
			.id(bookmarkId)
			.build();
		String deleteRequestJson = objectMapper.writeValueAsString(deleteRequest);

		// When Second
		ResultActions resultActions = mockMvc.perform(delete("/api-v1/bookmark/{postId}", otherPost.getId())
			.with(user(testSecurityUser))
			.content(deleteRequestJson)
			.header("Authorization", "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON));

		// Then Second
		resultActions.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("북마크 정보와 요청 게시물 정보가 다릅니다."));
	}
}

