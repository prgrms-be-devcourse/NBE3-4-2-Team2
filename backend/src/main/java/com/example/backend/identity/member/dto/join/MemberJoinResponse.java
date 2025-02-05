package com.example.backend.identity.member.dto.join;

import com.example.backend.entity.MemberEntity;


public record MemberJoinResponse(long id, String username) {
	public MemberJoinResponse(MemberEntity member) {
		this(member.getId(), member.getUsername());
	}
}
