package com.example.backend.social.reaction.likes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.entity.LikesRepository;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.entity.PostEntity;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.reaction.likes.dto.LikesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class LikesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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

		// 테스트용 멤버 추가 (id는 설정하지 않음)
		MemberEntity member = MemberEntity.builder()
			.username("testMember")
			.email("test@gmail.com")
			.password("testPassword")
			.build();
		testMember = memberRepository.save(member);

		// 테스트용 포스트 추가 (id는 설정하지 않음)
		PostEntity post = PostEntity.builder()
			.content("testContent")
			.member(member)
			.build();
		testPost = postRepository.save(post);
	}

	@Test
	public void testLikePost() throws Exception {

		LikesRequest likesRequest = new LikesRequest(testMember.getId(), testPost.getId());

		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(likesRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}

	@Test
	public void testUnlikePost() throws Exception {
		LikesRequest likesRequest = new LikesRequest(testMember.getId(), testPost.getId());

		mockMvc.perform(post("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(likesRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 적용되었습니다."))
			.andExpect(jsonPath("$.data").exists());

		mockMvc.perform(delete("/api-v1/likes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(likesRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.message").value("좋아요가 성공적으로 취소되었습니다."))
			.andExpect(jsonPath("$.data").exists());
	}
}
