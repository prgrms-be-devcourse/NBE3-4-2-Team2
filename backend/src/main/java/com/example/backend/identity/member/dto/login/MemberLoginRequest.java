package com.example.backend.identity.member.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MemberLoginRequest(
	@NotBlank
	@Schema(description = "사용자 이름", examples = "testUser")
	String username,

	@NotBlank(message = "비밀번호를 입력해주세요.")
	@Schema(description = "비밀번호",examples = "@test1234!@")
	String password
) {
}
