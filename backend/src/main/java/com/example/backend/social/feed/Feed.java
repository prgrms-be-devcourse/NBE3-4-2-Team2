package com.example.backend.social.feed;

import java.util.List;

import com.example.backend.entity.PostEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

/***
 * Feed
 * Feed Domain 내부의 비즈니스 로직에서 사용될 개념적 객체
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Data
@AllArgsConstructor
public class Feed {
	private PostEntity post;
	private Long likeCount;
	private Long commentCount;
	private List<String> hashTagList;
	private List<String> imageUrlList;

	private Long bookmarkId;

	public Feed(PostEntity post, Long likeCount, Long commentCount) {
		this.post = post;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
	}
}
