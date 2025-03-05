package com.example.backend.social.reaction.bookmark.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkListResponse(
	Long bookmarkId,
	Long postId,
	String postContent,  // 제목 대신 content 사용
	List<String> imageUrls, // 추가: 게시물 이미지 URL 목록
	LocalDateTime bookmarkedAt
) {}
