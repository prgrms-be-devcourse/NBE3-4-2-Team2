package com.example.backend.social.feed;

import java.util.List;

import com.example.backend.entity.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/***
 * Feed
 * Feed Domain 내부의 비즈니스 로직에서 사용될 개념적 객체
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Getter
@AllArgsConstructor
public class Feed {
	private PostEntity post;
	private Long commentCount;
	private List<String> hashTagList;
	private List<String> imageUrlList;
	private Long bookmarkId;
	private Boolean isLiked;

	public Feed(PostEntity post, Long commentCount) {
		this.post = post;
		this.commentCount = commentCount;
	}

	public void fillData(List<String> hashTagList, List<String> imageUrlList, Long bookmarkId, Boolean isLiked) {
		this.hashTagList = hashTagList;
		this.imageUrlList = imageUrlList;
		this.bookmarkId = bookmarkId;
		this.isLiked = isLiked;
	}
}
