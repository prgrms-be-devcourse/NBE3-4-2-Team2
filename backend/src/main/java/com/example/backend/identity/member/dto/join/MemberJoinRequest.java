package com.example.backend.identity.member.dto.join;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberJoinRequest(
	@NotBlank
	@Email(message = "Email 형식에 맞지 않습니다.")
	@Schema(description = "Email",examples = "test@naver.com")
	String email,
	@NotBlank
	@Pattern(regexp = "^(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
		message = "비밀번호는 10자 이상이며, 숫자와 특수문자를 포함해야 합니다.")
	@Schema(description = "비밀번호",examples = "@test1234!@")
	String password,
	@NotBlank
	@Schema(description = "사용자 이름",examples = "testUser")
	String username

	// @NotBlank
	// String phoneNumber // Todo : phoneNumber 추가로직을 추후로 구현해보기(프로필 페이지 이후)
) {
}
