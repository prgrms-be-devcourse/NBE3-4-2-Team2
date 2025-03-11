package com.example.backend.social.follow.dto;

import java.util.List;

import com.example.backend.entity.MemberEntity;

/**
 * 팔로잉 목록 조회 응답 DTO
 */
public record FollowingListResponse(
	List<FollowingMemberDto> followingList,
	int totalCount
) {
	public record FollowingMemberDto(
		Long id,
		String username,
		String profileUrl
	) {
		public static FollowingMemberDto from(MemberEntity member) {
			return new FollowingMemberDto(
				member.getId(),
				member.getUsername(),
				member.getProfileUrl()
			);
		}
	}

	public static FollowingListResponse of(List<FollowingMemberDto> followingList) {
		return new FollowingListResponse(followingList, followingList.size());
	}
}
