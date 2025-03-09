package com.example.backend.identity.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberResponse(
	@NotBlank
	long id,
	@NotBlank
	String username,
	String profileUrl,
	@NotBlank
	long postCount,
	@NotBlank
	long followerCount,
	@NotBlank
	long followingCount
) {
}
