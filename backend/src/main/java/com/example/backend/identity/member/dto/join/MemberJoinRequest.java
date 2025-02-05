package com.example.backend.identity.member.dto.join;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberJoinRequest(
	@NotBlank
	@Email(message = "Email 형식에 맞지 않습니다.")
	String email,
	@NotBlank
	@Pattern(regexp = "^(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{10,}$",
		message = "비밀번호는 10자 이상이며, 숫자와 특수문자를 포함해야 합니다.")
	String password,
	@NotBlank
	String username
) {
}
