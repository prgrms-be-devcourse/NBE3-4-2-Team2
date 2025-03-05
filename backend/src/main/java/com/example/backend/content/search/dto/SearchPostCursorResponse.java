package com.example.backend.content.search.dto;

import java.util.List;

import lombok.Builder;

/**
 * SearchPostResponse 에 lastPostId 와 마지막 페이지의 여부가 포함
 * @author kwak
 * 2025-02-07
 */
@Builder
public record SearchPostCursorResponse(
	List<SearchPostResponse> searchPostResponses,
	Long lastPostId,
	boolean hasNext
) {
	public static SearchPostCursorResponse create(
		List<SearchPostResponse> searchPostResponses, Long lastPostId, boolean hasNext) {

		return SearchPostCursorResponse.builder()
			.searchPostResponses(searchPostResponses)
			.lastPostId(lastPostId)
			.hasNext(hasNext)
			.build();
	}
}
