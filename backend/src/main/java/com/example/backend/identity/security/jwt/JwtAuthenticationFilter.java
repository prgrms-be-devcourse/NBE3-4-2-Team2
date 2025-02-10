package com.example.backend.identity.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.global.requestScope.Rq;
import com.example.backend.identity.member.service.MemberService;
import com.example.backend.identity.security.user.service.CustomUserDetailsService;
import com.example.backend.identity.security.user.CustomUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
	private final AccessTokenService accessTokenService;
	private final CustomUserDetailsService customUserDetailsService;


	private String getCookie(String key, Cookie[] cookies) {
		String value = Arrays.stream(cookies)
			.filter(cookie -> key.equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElse(null);

		return value;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
		ServletException, IOException {
		if (!request.getRequestURI().startsWith("/api-v1/")) {
			filterChain.doFilter(request, response);
			return;
		}

		if (List.of("/api-v1/members/login", "/api-v1/members/join").contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		// access 토큰이 없다면 다음 필터로 넘어감 (만료된 access 토큰이라도 있어야 refresh토큰 인증)
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String accessToken = authorization.substring("Bearer ".length());

		CustomUser customUser = customUserDetailsService.getUserByAccessToken(accessToken);

		// access 토큰이 유효하지 않다면 refresh 토큰을 이용하여 다시 생성
		if (customUser == null) {
			String refreshToken = getCookie("refresh_token", request.getCookies());

			// refresh 토큰이 존재하지 않다면 로그인 필터로 넘긴다.
			if (refreshToken == null) {
				filterChain.doFilter(request, response);
				return;
			}
			customUser = customUserDetailsService.getUserByRefreshToken(refreshToken);

			if (customUser == null) {
				filterChain.doFilter(request, response);
				return;
			}

			// refresh 토큰이 유효하다면 accessToken을 발급한다.
			String newAccessToken = accessTokenService.genAccessToken(customUser);
			response.setHeader("Authorization", "Bearer " + newAccessToken);
		}

		// 토큰이 유효하면 로그인 처리를 한다.
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}
