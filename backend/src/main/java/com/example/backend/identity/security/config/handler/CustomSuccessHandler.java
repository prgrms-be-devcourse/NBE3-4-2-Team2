package com.example.backend.identity.security.config.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.jwt.RefreshTokenService;
import com.example.backend.identity.security.user.CustomUser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 인증 성공 시 실행되는 핸들러.
 * <p>
 * 인증이 성공하면:
 * <ul>
 *     <li>기존 리프레시 토큰이 있을 경우 블랙리스트에 추가</li>
 *     <li>새로운 리프레시 토큰을 발급하고 쿠키에 저장</li>
 *     <li>새로운 액세스 토큰을 생성하고 응답 헤더에 추가</li>
 * </ul>
 * </p>
 *
 * @author k-haehchan
 * @since 25.01.10
 */
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;

	/**
	 * 사용자가 성공적으로 인증되었을 때 실행되는 메서드.
	 *
	 * @param request        클라이언트의 HTTP 요청 객체
	 * @param response       서버의 HTTP 응답 객체
	 * @param authentication 인증된 사용자 정보 객체
	 * @throws IOException      입출력 예외 발생 시
	 * @throws ServletException 서블릿 관련 예외 발생 시
	 *
	 * @author k-haechan
	 * @since 25.02.10
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
		throws IOException, ServletException {

		CustomUser loginUser = (CustomUser) authentication.getPrincipal();

		// 기존 리프레시 토큰이 있으면 블랙리스트에 추가 (정상적인 로직상 jwt 필터에서 로그인)
		Optional<String> existingRefreshToken = Optional.ofNullable(request.getCookies())
			.stream()
			.flatMap(Arrays::stream)
			.filter(cookie -> "refresh_token".equals(cookie.getName()))
			.map(Cookie::getValue)
			.findFirst();


		existingRefreshToken.ifPresent(refreshTokenService::addToBlacklist);

		// 새로운 리프레시 토큰 발급 및 쿠키 저장
		String newRefreshToken = refreshTokenService.genRefreshToken(loginUser);
		Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge((int) refreshTokenService.getRefreshTokenExpirationSeconds()); // 초 단위
		refreshTokenCookie.setHttpOnly(true); // XSS 공격 방지
		// refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 전송 가능하도록 설정
		response.addCookie(refreshTokenCookie);

		// 새로운 액세스 토큰 발급 및 응답 헤더 추가
		String accessToken = accessTokenService.genAccessToken(loginUser);
		response.setHeader("Authorization", "Bearer " + accessToken);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
