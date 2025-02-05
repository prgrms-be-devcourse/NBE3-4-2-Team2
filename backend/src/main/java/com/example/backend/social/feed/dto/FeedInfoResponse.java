package com.example.backend.social.feed.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.social.feed.Feed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 피드 정보 DTO
 * 요청한 피드에 대한 단건 정보를 담은 객체
 *
 * @author ChoiHyunSan
 * @since 2025-02-03
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedInfoResponse {

	// 작성자 정보
	private Long authorId;
	private String authorName;

	// 게시글 정보
	private Long postId;
	private List<String> imgUrlList;
	private String content;
	private Long likesCount;
	private Long commentCount;
	private LocalDateTime createdDate;

	// 헤시태그 정보
	private List<String> hashTagList;

	// 북마크 여부
	private Long bookmarkId;

	public static FeedInfoResponse toResponse(Feed feed) {
		return FeedInfoResponse.builder()
			.authorId(feed.getPost().getMember().getId())
			.authorName(feed.getPost().getMember().getUsername())
			.imgUrlList(feed.getImageUrlList())
			.postId(feed.getPost().getId())
			.content(feed.getPost().getContent())
			.likesCount(feed.getLikeCount())
			.commentCount(feed.getCommentCount())
			.createdDate(feed.getPost().getCreateDate())
			.hashTagList(feed.getHashTagList())
			.bookmarkId(feed.getBookmarkId())
			.build();
	}
}
