package com.example.backend.content.search.dto;

import lombok.Builder;

/**
 * @author kwak
 * 2025. 2. 21.
 */

@Builder
public record ImageDataWithPostId(
	Long postId,
	String imgUrl
) {
	public static ImageDataWithPostId create(Long postId, String imgUrl) {
		return ImageDataWithPostId.builder()
			.postId(postId)
			.imgUrl(imgUrl)
			.build();
	}
}
