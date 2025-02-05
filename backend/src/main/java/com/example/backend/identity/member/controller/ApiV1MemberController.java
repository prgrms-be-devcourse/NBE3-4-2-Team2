package com.example.backend.identity.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.exception.GlobalException;
import com.example.backend.global.requestScope.Rq;
import com.example.backend.global.rs.RsData;
import com.example.backend.identity.member.dto.MemberResponse;
import com.example.backend.identity.member.dto.join.MemberJoinRequest;
import com.example.backend.identity.member.dto.login.MemberLoginRequest;
import com.example.backend.identity.member.dto.join.MemberJoinResponse;
import com.example.backend.identity.member.dto.login.MemberLoginResponse;
import com.example.backend.identity.member.exception.MemberErrorCode;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.user.SecurityUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api-v1/members")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApiV1MemberController {
	private final MemberService memberService;
	private final Rq rq;


	@PostMapping(value = "/join")
	@Transactional
	public ResponseEntity<RsData<MemberJoinResponse>> join(
		@RequestBody @Valid MemberJoinRequest reqBody
	) {
		MemberEntity member = memberService.join(reqBody.username(), reqBody.password(), reqBody.email());

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(
					RsData.success(
						new MemberJoinResponse(member),
						"%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getUsername()))
				);
	}


	@PostMapping("/login")
	public ResponseEntity<RsData<MemberLoginResponse>> login(
		@RequestBody @Valid MemberLoginRequest reqBody
	) {
		MemberEntity member = memberService.findByUsername(reqBody.username())
								.orElseThrow(() -> new GlobalException(
									MemberErrorCode.NOT_FOUND,
									"사용자 정보가 존재하지 않습니다."
								));

		if (!memberService.matchPassword(member, reqBody.password()))
			throw new GlobalException(MemberErrorCode.INVALID_PASSWORD);

		String accessToken = memberService.genAccessToken(member);

		rq.setHeader("Authorization", "Bearer " + member.getRefreshToken() + " " + accessToken);

		rq.setCookie("access_token", accessToken);
		rq.setCookie("refresh_token", member.getRefreshToken());

		return ResponseEntity.ok(RsData.success(
			new MemberLoginResponse(member),
			"%s님 환영합니다.".formatted(member.getUsername())
		));
	}


	@DeleteMapping("/logout")
	public ResponseEntity<RsData<Void>> logout() {
		rq.deleteCookie("access_token");
		rq.deleteCookie("refresh_token");

		return ResponseEntity.ok(RsData.success(
			null,
			"로그아웃 되었습니다."
		));
	}


	@GetMapping("/{id}")
	public ResponseEntity<RsData<MemberResponse>> publicMemberDetails(@PathVariable("id") long id, @AuthenticationPrincipal SecurityUser securityUser) {
		MemberEntity member = memberService.findById(id)
			.orElseThrow(
				()-> new GlobalException(
					MemberErrorCode.NOT_FOUND
			)
		);

		return ResponseEntity.ok(
			RsData.success(
				new MemberResponse(
					member.getId(),
					member.getUsername(),
					member.getProfileUrl(),
					member.getFollowerCount(),
					member.getFollowingCount()
				),
				"%s님의 정보 입니다.".formatted(member.getUsername())
			)
		);
	}
}
