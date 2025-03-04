package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

/**
 * 피드 정보 DTO
 * 요청한 피드에 대한 단건 정보를 담은 객체
 *
 * @author ChoiHyunSan
 * @since 2025-02-03
 */
@Builder
public record FeedInfoResponse(
	// 작성자 정보
	Long authorId,
	String authorName,

	// 게시글 정보
	Long postId,
	List<String> imgUrlList,
	String content,
	Long likeCount,
	Long commentCount,
	LocalDateTime createdDate,

	// 헤시태그 정보
	List<String> hashTagList,

	// 북마크 여부
	Long bookmarkId
) {
}
