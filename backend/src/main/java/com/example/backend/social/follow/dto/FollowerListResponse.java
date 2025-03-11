package com.example.backend.social.follow.dto;

import java.util.List;

import com.example.backend.entity.MemberEntity;

/**
 * 팔로워 목록 조회 응답 DTO
 */
public record FollowerListResponse(
	List<FollowerMemberDto> followerList,
	int totalCount
) {
	public record FollowerMemberDto(
		Long id,
		String username,
		String profileUrl
	) {
		public static FollowerMemberDto from(MemberEntity member) {
			return new FollowerMemberDto(
				member.getId(),
				member.getUsername(),
				member.getProfileUrl()
			);
		}
	}

	public static FollowerListResponse of(List<FollowerMemberDto> followerList) {
		return new FollowerListResponse(followerList, followerList.size());
	}
}
