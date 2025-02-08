package com.example.backend.content.search.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.content.search.dto.SearchPostCursorResponse;
import com.example.backend.content.search.service.SearchService;
import com.example.backend.content.search.type.SearchType;
import com.example.backend.global.rs.RsData;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * type 은 author 와 hashtag 두가지로 나눠서 검색 가능
 * 처음에 lastPostId 는 null
 * @author kwak
 * 2025-02-06
 */
@RestController
@RequestMapping("/api-v1/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@ResponseStatus(HttpStatus.OK)
	@GetMapping
	public RsData<SearchPostCursorResponse> search(
		@RequestParam @NotNull(message = "검색 타입은 필수입니다.") SearchType type,
		@RequestParam @NotBlank(message = "검색어는 필수입니다.") String keyword,
		@RequestParam(required = false) Long lastPostId,
		@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
	) {
		SearchPostCursorResponse response = searchService.search(type, keyword, lastPostId, size);
		return RsData.success(response);
	}
}
