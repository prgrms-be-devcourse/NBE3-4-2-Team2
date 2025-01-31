package com.example.backend.social.feed;

import java.util.List;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;

import lombok.Data;

/***
 * Feed
 * Feed Domain 내부의 비즈니스 로직에서 사용될 개념적 객체
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Data
public class Feed {
	private MemberEntity author;
	private PostEntity post;
	private List<String> hashTagList;
	private List<String> imageUrlList;
	private int likeCount;
	private int commentCount;
}
