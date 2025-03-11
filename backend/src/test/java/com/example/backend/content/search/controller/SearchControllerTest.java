package com.example.backend.content.search.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.content.search.dto.SearchPostCursorResponse;
import com.example.backend.content.search.dto.SearchPostResponse;
import com.example.backend.content.search.type.SearchType;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.jwt.RefreshTokenService;
import com.example.backend.identity.security.user.service.CustomUserDetailsService;

/**
 * @author kwak
 * 2025-02-12
 */

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

	@Autowired
	MockMvc mockMvc;
	@MockitoBean
	SearchService searchService;
	@MockitoBean
	AccessTokenService accessTokenService;
	@MockitoBean
	CustomUserDetailsService customUserDetailsService;
	@MockitoBean
	RefreshTokenService refreshTokenService;

	@Test
	@DisplayName("검색 조회 목록")
	void test1() throws Exception {
		// 준비(Arrange)
		SearchType searchType = SearchType.HASHTAG;
		String keyword = "강아지";
		Long lastPostId = 0L;
		int size = 10;

		SearchPostResponse searchPostResponse1 = SearchPostResponse.builder()
			.postId(1L)
			.imageUrl("a.jpg")
			.build();

		SearchPostResponse searchPostResponse2 = SearchPostResponse.builder()
			.postId(2L)
			.imageUrl("b.jpg")
			.build();

		SearchPostResponse searchPostResponse3 = SearchPostResponse.builder()
			.postId(3L)
			.imageUrl("c.jpg")
			.build();

		SearchPostCursorResponse response = SearchPostCursorResponse.builder()
			.searchPostResponses(
				List.of(
					searchPostResponse1, searchPostResponse2, searchPostResponse3))
			.hasNext(false)
			.lastPostId(1L)
			.build();

		// 검색 서비스 모의 설정
		when(searchService.search(searchType, keyword, lastPostId, size))
			.thenReturn(response);

		mockMvc.perform(
			get("/api-v1/search")
				.param("type", searchType.name())
				.param("keyword", keyword)
				.param("lastPostId", String.valueOf(lastPostId))
				.param("size", String.valueOf(size))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.searchPostResponses", hasSize(3)))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.lastPostId").value(1));
	}
}
