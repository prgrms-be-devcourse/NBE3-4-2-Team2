package com.example.backend.identity.security.config.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.backend.identity.security.jwt.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 로그아웃 시 실행되는 핸들러
 * @author k-haechn
 * @since 25.01.10
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
	private final RefreshTokenService refreshTokenService;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("refresh_token")) {
				refreshTokenService.addToBlacklist(cookie.getValue()); // 기존 refreshToken 삭제
			}
		}

		Cookie refreshTokenCookie = new Cookie("refresh_token", null);
		refreshTokenCookie.setMaxAge(0);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setHttpOnly(true);
		response.addCookie(refreshTokenCookie);

		// access 토큰 삭제
		response.setHeader("Authorization", null); // 일부 클라이언트에서 효과적
		response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 일부 클라이언트에서는 헤더 초기화를 함
	}
}
