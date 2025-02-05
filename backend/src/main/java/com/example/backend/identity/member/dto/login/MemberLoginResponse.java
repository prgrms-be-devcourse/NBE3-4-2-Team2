package com.example.backend.identity.member.dto.login;

import com.example.backend.entity.MemberEntity;


public record MemberLoginResponse(long id, String username, String profileUrl) {
	public MemberLoginResponse(MemberEntity member) {
		this(member.getId(), member.getUsername(), member.getProfileUrl());
	}
}
