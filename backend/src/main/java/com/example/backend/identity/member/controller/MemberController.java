package com.example.backend.identity.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.global.rs.RsData;
import com.example.backend.identity.member.dto.MemberResponse;
import com.example.backend.identity.member.dto.join.MemberJoinRequest;
import com.example.backend.identity.member.dto.join.MemberJoinResponse;
import com.example.backend.identity.member.exception.MemberErrorCode;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.user.SecurityUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api-v1/members")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@PostMapping(value = "/join")
	@Transactional
	@ResponseStatus(HttpStatus.OK)
	public RsData<MemberJoinResponse> join(
		@RequestBody @Valid MemberJoinRequest reqBody
	) {
		MemberEntity member = memberService.join(reqBody.username(), reqBody.password(), reqBody.email());

		return RsData.success(
						new MemberJoinResponse(member),
						"%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getUsername()));
	}

	// @DeleteMapping("/logout")
	// @ResponseStatus(HttpStatus.OK)
	// public RsData<Void> logout() {
	// 	rq.deleteCookie("access_token");
	// 	rq.deleteCookie("refresh_token");
	//
	// 	return RsData.success(
	// 		null,
	// 		"로그아웃 되었습니다.");
	// }


	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public RsData<MemberResponse> publicMemberDetails(@PathVariable("id") long id, @AuthenticationPrincipal SecurityUser securityUser) {
		MemberEntity member = memberService.findById(id)
			.orElseThrow(
				()-> new GlobalException(
					MemberErrorCode.NOT_FOUND
			)
		);

		return RsData.success(
				new MemberResponse(
					member.getId(),
					member.getUsername(),
					member.getProfileUrl(),
					member.getFollowerCount(),
					member.getFolloweeCount()
				),
				"%s님의 정보 입니다.".formatted(member.getUsername()));
	}
}
