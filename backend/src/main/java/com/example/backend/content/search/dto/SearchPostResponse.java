package com.example.backend.content.search.dto;

import lombok.Builder;

/**
 * 검색 시 간단한 이미지 결과와 연결한 postId 를 반환하는 response
 * 게시글의 첫번째 이미지만 받아서 반환
 * @author kwak
 * 2025-02-07
 */
@Builder
public record SearchPostResponse(
	Long postId,
	String imageUrl
) {
	public static SearchPostResponse create(Long postId, String imageUrl) {
		return SearchPostResponse.builder()
			.postId(postId)
			.imageUrl(imageUrl)
			.build();
	}
}
