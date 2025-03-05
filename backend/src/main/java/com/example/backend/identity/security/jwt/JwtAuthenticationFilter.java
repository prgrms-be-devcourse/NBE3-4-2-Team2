package com.example.backend.identity.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.identity.security.user.service.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>쿠키나 헤더를 통한 Jwt 인증을 담당하는 필터</p>
 * @author KimHaeChan
 * @since 25. 2. 3
 * */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final AccessTokenService accessTokenService;
	private final CustomUserDetailsService customUserDetailsService;
	private final RefreshTokenService refreshTokenService;

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

		String accessToken = getAccessToken(request);
		String refreshToken = getRefreshToken(request);

		if (accessToken == null && refreshToken == null) {
			filterChain.doFilter(request, response);
			return;
		}

		CustomUser user  = null;
		if(accessToken != null) {
			user = customUserDetailsService.getUserByAccessToken(accessToken);
		}

		// accessToken이 유효하지 않다면 Refresh토큰을 이용하여 로그인 처리를 한다.
		if (user == null) {
			user = customUserDetailsService.getUserByRefreshToken(refreshToken);
			if (user == null) {
				filterChain.doFilter(request, response);
				return;
			}
			// 헤더에 accessToken을 추가
			accessTokenService.genAccessToken(user, response);
			log.error("accessToken 재발급");
		}

		// 로그인 처리
		user.setLogin();
		filterChain.doFilter(request, response);
	}

	private String getRefreshToken(HttpServletRequest request) {
		String refreshToken = null;
		if(request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("refresh_token")) {
				refreshToken = cookie.getValue();
				break;
			}
		}
		return refreshTokenService.isBlacklisted(refreshToken)? null : refreshToken;
	}

	private String getAccessToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		return authorization == null || !authorization.startsWith("Bearer ") ? null : authorization.substring("Bearer ".length());
	}
}
