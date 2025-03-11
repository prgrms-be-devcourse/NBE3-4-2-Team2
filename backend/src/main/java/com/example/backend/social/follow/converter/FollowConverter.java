package com.example.backend.social.follow.converter;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.entity.MemberEntity;
import com.example.backend.social.follow.dto.FollowResponse;
import com.example.backend.social.follow.dto.FollowerListResponse;
import com.example.backend.social.follow.dto.FollowingListResponse;

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

	/**
	 * 팔로잉 목록 응답 객체 생성
	 */
	public static FollowingListResponse toFollowingListResponse(List<MemberEntity> followingMembers) {
		List<FollowingListResponse.FollowingMemberDto> dtoList = followingMembers.stream()
			.map(FollowingListResponse.FollowingMemberDto::from)
			.toList();

		return FollowingListResponse.of(dtoList);
	}

	/**
	 * 팔로워 목록 응답 객체 생성
	 */
	public static FollowerListResponse toFollowerListResponse(List<MemberEntity> followerMembers) {
		List<FollowerListResponse.FollowerMemberDto> dtoList = followerMembers.stream()
			.map(FollowerListResponse.FollowerMemberDto::from)
			.toList();

		return FollowerListResponse.of(dtoList);
	}
}
