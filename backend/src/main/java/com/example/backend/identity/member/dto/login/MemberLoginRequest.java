package com.example.backend.identity.member.dto.login;

import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
	@NotBlank
	String username,
	@NotBlank(message = "비밀번호를 입력해주세요.")
	String password
) {
}
