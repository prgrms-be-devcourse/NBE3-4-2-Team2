package com.example.backend.social.reaction.like.converter;

import java.time.LocalDateTime;

import com.example.backend.social.reaction.like.dto.LikeInfo;
import com.example.backend.social.reaction.like.dto.LikeToggleResponse;

public class LikeConverter {

	/**
	 * LikeInfo 객체와 likeCount를 사용하여 LikeToggleResponse 생성
	 *
	 * @param likeInfo, likeCount
	 * @return LikeToggleResponse
	 */
	public static LikeToggleResponse toLikeResponse(LikeInfo likeInfo, Long likeCount) {
		return new LikeToggleResponse(
			likeInfo.memberId(),
			likeInfo.resourceId(),
			likeInfo.resourceType(),
			likeInfo.isActive(),
			likeCount,
			LocalDateTime.now()
		);
	}
}
