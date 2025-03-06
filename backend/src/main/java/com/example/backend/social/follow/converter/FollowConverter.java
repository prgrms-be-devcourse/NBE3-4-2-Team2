package com.example.backend.social.follow.converter;

import java.time.LocalDateTime;

import com.example.backend.entity.MemberEntity;
import com.example.backend.social.follow.dto.FollowResponse;

public class FollowConverter {
	/**
	 * 팔로우 응답 DTO 변환 메서드
	 * FollowResponse에 타임스탬프(now) 추가해 변환
	 *
	 * @param sender, receiver
	 * @return CreateFollowResponse
	 */
	public static FollowResponse toResponse(MemberEntity sender, MemberEntity receiver) {
		return new FollowResponse(
			sender.getUsername(),
			receiver.getUsername(),
			LocalDateTime.now()
		);
	}
}
