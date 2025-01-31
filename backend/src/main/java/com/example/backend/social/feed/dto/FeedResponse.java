package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;

@Getter
public class FeedResponse {

	// 작성자 정보
	private Long authorId;
	private String authorName;

	// 게시글 정보
	private Long postId;
	private List<String> imgUrlList;
	private String content;
	private int likesCount;
	private int commentCount;
	private LocalDateTime createdDate;

	// 헤시태그 정보
	private List<String> hashTagList;
}
