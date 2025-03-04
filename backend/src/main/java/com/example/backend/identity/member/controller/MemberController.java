package com.example.backend.identity.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.example.backend.identity.member.dto.login.MemberLoginRequest;
import com.example.backend.identity.member.dto.login.MemberLoginResponse;
import com.example.backend.identity.member.exception.MemberErrorCode;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.jwt.RefreshTokenService;
import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.identity.security.user.SecurityUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api-v1/members")
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Tag(name = "MemberController", description = "회원 컨트롤러")
public class MemberController {
	private final MemberService memberService;
	private final RefreshTokenService refreshTokenService;
	private final AccessTokenService accessTokenService;

	@PostMapping(value = "/join")
	@Transactional
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "회원가입")
	public RsData<MemberJoinResponse> join(
		@RequestBody @Valid MemberJoinRequest reqBody
	) {
		MemberEntity member = memberService.join(reqBody.username(), reqBody.password(), reqBody.email());

		return RsData.success(
						new MemberJoinResponse(member),
						"%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getUsername()));
	}

	@PostMapping(value = "/login")
	@Transactional
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "로그인")
	public RsData<MemberLoginResponse> login(
		@RequestBody @Valid MemberLoginRequest reqBody,
		HttpServletResponse response
	) {
		// 로그인 처리
		CustomUser loginUser = memberService.login(reqBody.username(), reqBody.password());

		// 헤더로 액세스 토큰 발급
		accessTokenService.genAccessToken(loginUser, response);

		// 쿠키로 리프레시 토큰 발급
		refreshTokenService.genRefreshToken(loginUser, response);

		return RsData.success(
				null, // Front에서는 JWT 토큰을 통해 유저 정보를 획득할 수 있음.
					"%s님 환영합니다.".formatted(loginUser.getUsername())
		);
	}

	@DeleteMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "로그아웃")
	@SecurityRequirement(name = "bearerAuth")
	public RsData<Void> logout(HttpServletRequest request, HttpServletResponse response) {

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("refresh_token")) {
				refreshTokenService.addToBlacklist(cookie.getValue()); // 기존 refreshToken을 통한 접근 불허
			}
		}

		ResponseCookie responseCookie = ResponseCookie.from("refresh_token", "")
			.path("/")
			.maxAge(0)
			.httpOnly(true)
			.sameSite("Lax")
			.build();
		response.addHeader("Set-Cookie", responseCookie.toString());

		// access 토큰 삭제(프론트엔드에서 따로 설정해주긴 해야함)
		response.setHeader("Authorization", null); // 일부 클라이언트에서 효과적
		response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 일부 클라이언트에서는 헤더 초기화를 하면 Bearer 토큰이 삭제됨, 근데 어차피 ResponseStatus.Ok로 응답을 보내기 때문에 의미 없음

		return RsData.success(
			null,
			"로그아웃 되었습니다.");
	}


	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "회원 정보 조회")
	@SecurityRequirement(name = "bearerAuth")
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

