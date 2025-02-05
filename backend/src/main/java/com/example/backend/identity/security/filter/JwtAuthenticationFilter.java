package com.example.backend.identity.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.requestScope.Rq;
import com.example.backend.identity.member.service.JwtService;
import com.example.backend.identity.member.service.MemberService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * <p>쿠키나 헤더를 통한 Jwt 인증을 담당하는 필터</p>
 * @author KimHaeChan
 * @since 25. 2. 3
 * */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final MemberService memberService;
	private final Rq rq;


	record AuthTokens(String refreshToken, String accessToken) {
	}

	private AuthTokens getAuthTokensFromRequest() {
		String authorization = rq.getHeader("Authorization");

		if (authorization != null && authorization.startsWith("Bearer ")) {
			String token = authorization.substring("Bearer ".length());
			String[] tokenBits = token.split(" ", 2);

			if (tokenBits.length == 2)
				return new AuthTokens(tokenBits[0], tokenBits[1]);
		}

		String refreshToken = rq.getCookieValue("refresh_token");
		String accessToken = rq.getCookieValue("access_token");

		if (refreshToken != null && accessToken != null)
			return new AuthTokens(refreshToken, accessToken);

		return null;
	}


	private void refreshAccessToken(MemberEntity member) {
		String newAccessToken = memberService.genAccessToken(member);

		rq.setHeader("Authorization", "Bearer " + member.getRefreshToken() + " " + newAccessToken);
		rq.setCookie("access_token", newAccessToken);
	}

	private MemberEntity refreshAccessTokenByRefreshToken(String refreshToken) {
		Optional<MemberEntity> opMemberByRefreshToken = memberService.findByRefreshToken(refreshToken);

		if (opMemberByRefreshToken.isEmpty()) {
			return null;
		}

		MemberEntity member = opMemberByRefreshToken.get();

		refreshAccessToken(member);

		return member;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
		ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api-v1/")) {
			filterChain.doFilter(request, response);
			return;
		}

		if (List.of("/api-v1/members/login", "/api-v1/members/logout", "/api-v1/members/join").contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		AuthTokens authTokens = getAuthTokensFromRequest();

		if (authTokens == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String refreshToken = authTokens.refreshToken;
		String accessToken = authTokens.accessToken;

		MemberEntity member = memberService.getActorFromAccessToken(accessToken);

		if (member == null)
			member = refreshAccessTokenByRefreshToken(refreshToken);

		if (member != null)
			rq.setLogin(member);

		filterChain.doFilter(request, response);
	}
}
