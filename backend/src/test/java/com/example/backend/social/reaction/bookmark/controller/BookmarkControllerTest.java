package com.example.backend.social.reaction.bookmark.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.entity.BookmarkRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.bookmark.dto.CreateBookmarkRequest;
import com.example.backend.social.reaction.bookmark.dto.DeleteBookmarkRequest;
import com.example.backend.social.reaction.likes.dto.LikesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class BookmarkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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
	@DisplayName("1. 북마크 생성 테스트")
	public void t001() throws Exception {
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

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
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("북마크가 성공적으로 추가되었습니다."))
			.andExpect(jsonPath("$.data").exists());

		DeleteBookmarkRequest deleteRequest = new DeleteBookmarkRequest(1L, testMember.getId(), testPost.getId());
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
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(99L, testPost.getId());

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
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), 99L);

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
		CreateBookmarkRequest createRequest = new CreateBookmarkRequest(testMember.getId(), testPost.getId());

		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		mockMvc.perform(post("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("이미 등록된 북마크 입니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}
}

	@Test
	@DisplayName("6. 좋아요가 없는 게시물에 좋아요 취소 테스트")
	public void t006() throws Exception {
		LikesRequest likesRequest = new LikesRequest(testMember.getId(), testPost.getId());

		mockMvc.perform(delete("/api-v1/bookmark")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(likesRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.message").value("좋아요 정보를 찾을 수 없습니다."))
			.andExpect(jsonPath("$.data").isEmpty());
	}
}

