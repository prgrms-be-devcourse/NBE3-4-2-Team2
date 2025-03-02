package com.example.backend.identity.member.dto;

public record MemberResponse(
	long id,
	String username,
	String profileUrl,
	long followerCount,
	long followingCount
) {
}
