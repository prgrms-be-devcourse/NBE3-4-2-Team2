package com.example.backend.social.reaction.bookmark.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

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

import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
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
	private MemberRepository memberRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private BookmarkRepository bookmarkRepository;

	private MemberEntity testMember;
	private PostEntity testPost;

	@BeforeEach
	public void setup() {
		// 테스트 전에 데이터 초기화
		bookmarkRepository.deleteAll();
		postRepository.deleteAll();
		memberRepository.deleteAll();

		// 시퀀스 초기화 (테스트 데이터 재 생성시 아이디 값이 올라가기 때문)
		entityManager.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE post ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE bookmark ALTER COLUMN id RESTART WITH 1").executeUpdate();

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
	@DisplayName("1. 북마크 생성 테스트")
	public void t001() throws Exception {
		// Given
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		// When & Then
		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 추가되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("2. 북마크 삭제 테스트")
	public void t002() throws Exception {
		// Given First
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		// When & Then First
		MvcResult result = mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 추가되었습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String jsonResponse = result.getResponse().getContentAsString();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		Long bookmarkId = rootNode.path("data").path("id").asLong();
		DeleteBookmarkRequest deleteRequest = new DeleteBookmarkRequest(bookmarkId, testMember.getId(), testPost.getId());

		// When & Then Second
		mockMvc.perform(delete("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 제거되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	@DisplayName("3. 존재하지 않는 멤버가 북마크 등록 테스트")
	public void t003() throws Exception {
		// Given
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(99L, testPost.getId());

		// When & Then
		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("멤버 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("4. 존재하지 않는 게시물을 북마크 등록 테스트")
	public void t004() throws Exception {
		// Given
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), 99L);

		// When & Then
		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("게시물 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("5. 북마크가 이미 등록된 게시물에 북마크 등록 테스트")
	public void t005() throws Exception {
		// Given
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		// Success When & Then
		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// Fail When & Then
		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 등록된 북마크 입니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("6. 북마크 등록이 안된 게시물에 북마크 삭제 테스트")
	public void t006() throws Exception {
		// Given
		DeleteBookmarkRequest deleteRequest = new DeleteBookmarkRequest(1L, testMember.getId(), testPost.getId());

		// When & Then
		mockMvc.perform(delete("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("북마크 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("7. 북마크 삭제시 다른 유저가 요청하는 테스트")
	public void t007() throws Exception {
		// Given First
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		// When & Then First
		MvcResult result = mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 추가되었습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String jsonResponse = result.getResponse().getContentAsString();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		Long bookmarkId = rootNode.path("data").path("id").asLong();
		DeleteBookmarkRequest deleteRequest = new DeleteBookmarkRequest(bookmarkId, 2L, testPost.getId());

		// When & Then Second
		mockMvc.perform(delete("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("북마크에 접근할 권한이 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}

	@Test
	@DisplayName("8. DB에 등록된 북마크와 해당 멤버가 다른 경우 테스트")
	public void t008() throws Exception {
		// Given First
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		// When & Then First
		MvcResult result = mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 추가되었습니다."))
			.andExpect(jsonPath("$.data").exists())
			.andReturn();

		// Given Second
		String jsonResponse = result.getResponse().getContentAsString();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		Long bookmarkId = rootNode.path("data").path("id").asLong();
		DeleteBookmarkRequest deleteRequest = new DeleteBookmarkRequest(bookmarkId, testMember.getId(), 2L);

		// When & Then Second
		mockMvc.perform(delete("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(deleteRequest)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("북마크 정보와 요청 게시물 정보가 다릅니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}
}

