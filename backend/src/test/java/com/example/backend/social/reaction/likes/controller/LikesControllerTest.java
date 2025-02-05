package com.example.backend.social.reaction.likes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.backend.entity.LikesRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.likes.dto.CreateLikeRequest;
import com.example.backend.social.reaction.likes.dto.DeleteLikeRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class LikesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private LikesRepository likesRepository;

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
	public void t001() throws Exception {
		// Given
		CreateLikeRequest createRequest = new CreateLikeRequest(testMember.getId(), testPost.getId());

		// When & Then
		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("2. 좋아요 취소 테스트")
	public void t002() throws Exception {
		// Given First
		CreateLikeRequest createRequest = new CreateLikeRequest(testMember.getId(), testPost.getId());

		// When & Then First
		MvcResult result = mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String jsonResponse = result.getResponse().getContentAsString();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		Long likeId = rootNode.path("data").path("id").asLong();
		DeleteLikeRequest deleteRequest = new DeleteLikeRequest(likeId, testMember.getId(), testPost.getId());

		// When & Then Second
		mockMvc.perform(delete("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 취소되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버가 좋아요 적용 테스트")
	public void t003() throws Exception {
		// Given
		CreateLikeRequest createRequest = new CreateLikeRequest(99L, testPost.getId());

		// When & Then
		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("멤버 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물에 좋아요 적용 테스트")
	public void t004() throws Exception {
		// Given
		CreateLikeRequest createRequest = new CreateLikeRequest(testMember.getId(), 99L);

		// When & Then
		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("게시물 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("5. 좋아요가 이미 적용된 게시물에 좋아요 중복 적용 테스트")
	public void t005() throws Exception {
		// Given
		CreateLikeRequest createRequest = new CreateLikeRequest(testMember.getId(), testPost.getId());

		// Success When & Then
		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// Fail When & Then
		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("6. 좋아요를 누르지 않은 게시물에 좋아요 취소 요청 테스트")
	public void t006() throws Exception {
		// Given
		DeleteLikeRequest deleteRequest = new DeleteLikeRequest(1L, testMember.getId(), testPost.getId());

		// When & Then
		mockMvc.perform(delete("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("7. 좋아요 취소시 다른 유저가 요청하는 테스트")
	public void t007() throws Exception {
		// Given First
		CreateLikeRequest createRequest = new CreateLikeRequest(testMember.getId(), testPost.getId());

		// When & Then First
		MvcResult result = mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String jsonResponse = result.getResponse().getContentAsString();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		Long likeId = rootNode.path("data").path("id").asLong();
		DeleteLikeRequest deleteRequest = new DeleteLikeRequest(likeId, 2L, testPost.getId());

		// When & Then Second
		mockMvc.perform(delete("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요를 취소할 권한이 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("8. DB에 등록된 좋아요와 해당 멤버가 다른 경우 테스트")
	public void t008() throws Exception {
		// Given First
		CreateLikeRequest createRequest = new CreateLikeRequest(testMember.getId(), testPost.getId());

		// When & Then First
		MvcResult result = mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String jsonResponse = result.getResponse().getContentAsString();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		Long likeId = rootNode.path("data").path("id").asLong();
		DeleteLikeRequest deleteRequest = new DeleteLikeRequest(likeId, testMember.getId(), 2L);

		// When & Then Second
		mockMvc.perform(delete("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요 정보와 요청 게시물 정보가 다릅니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}
}
