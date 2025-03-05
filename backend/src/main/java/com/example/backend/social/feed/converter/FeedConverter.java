package com.example.backend.social.feed.converter;

import org.springframework.stereotype.Component;

import com.example.backend.social.feed.Feed;
import com.example.backend.social.feed.dto.FeedInfoResponse;

/**
 * FeedConverter
 * 피드 관련 객체로 변환하는 역할을 담당하는 클래스
 *
 * @author ChoiHyunSan
 * @since 2025-02-06
 */
@Component
public class FeedConverter {

	public FeedInfoResponse toFeedInfoResponse(Feed feed) {
		return FeedInfoResponse.builder()
			.authorId(feed.getPost().getMember().getId())
			.authorName(feed.getPost().getMember().getUsername())
			.imgUrlList(feed.getImageUrlList())
			.postId(feed.getPost().getId())
			.content(feed.getPost().getContent())
			.likeCount(feed.getPost().getLikeCount())
			.commentCount(feed.getCommentCount())
			.createdDate(feed.getPost().getCreateDate())
			.hashTagList(feed.getHashTagList())
			.bookmarkId(feed.getBookmarkId())
			.build();
	}

}
