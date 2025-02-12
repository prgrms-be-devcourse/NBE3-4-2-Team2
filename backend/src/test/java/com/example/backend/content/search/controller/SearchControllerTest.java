package com.example.backend.content.search.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.content.search.dto.SearchPostCursorResponse;
import com.example.backend.content.search.dto.SearchPostResponse;
import com.example.backend.content.search.service.SearchService;
import com.example.backend.content.search.type.SearchType;
import com.example.backend.global.rs.RsData;

/**
 * @author kwak
 * 2025-02-12
 */

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

	@Mock
	private SearchService searchService;

	@InjectMocks
	private SearchController searchController;

	private SearchPostCursorResponse mockResponse;

	@BeforeEach
	void setUp() {
		// 모의 검색 응답 생성
		mockResponse = SearchPostCursorResponse.builder()
			.searchPostResponses(List.of(
				SearchPostResponse.builder()
					.postId(1L)
					.imageUrl("a.jpg")
					.build()
			))
			.hasNext(false)
			.lastPostId(1L)
			.build();
	}

	@Test
	void search_SuccessfulSearch_ReturnsExpectedResponse() {
		// 준비(Arrange)
		SearchType searchType = SearchType.HASHTAG;
		String keyword = "테스트";
		Long lastPostId = null;
		int size = 10;

		// 검색 서비스 모의 설정
		when(searchService.search(searchType, keyword, lastPostId, size))
			.thenReturn(mockResponse);

		// 실행(Act)
		RsData<SearchPostCursorResponse> result = searchController.search(searchType, keyword, lastPostId, size);

		// 검증(Assert)
		assertNotNull(result);
		assertTrue(result.isSuccess());
		assertEquals(mockResponse, result.getData());

		// 검색 서비스 호출 확인
		verify(searchService).search(searchType, keyword, lastPostId, size);
	}
}
