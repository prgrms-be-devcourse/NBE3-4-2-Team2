package com.example.backend.identity.security.config.handler;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.backend.global.rs.RsData;
import com.example.backend.global.util.JsonUtil;
import com.example.backend.identity.member.dto.login.MemberLoginResponse;
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
 * @author k-haehchan
 * @since 25.01.10
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
		throws IOException, ServletException {

		CustomUser loginUser = (CustomUser) authentication.getPrincipal();

		// 새로운 리프레시 토큰 발급 및 쿠키 저장
		String refreshToken = refreshTokenService.genRefreshToken(loginUser);

		Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge((int) refreshTokenService.getRefreshTokenExpirationSeconds()); // 초 단위
		refreshTokenCookie.setHttpOnly(true); // XSS 공격 방지
		refreshTokenCookie.setSecure(request.isSecure()); // 요청의 형태에 따라 동일한 반환 형태 설정
		response.addCookie(refreshTokenCookie);

		// 새로운 액세스 토큰 발급 및 응답 헤더 추가
		String accessToken = accessTokenService.genAccessToken(loginUser);
		response.setHeader("Authorization", "Bearer " + accessToken);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");


		RsData<MemberLoginResponse> success = RsData.success(
			new MemberLoginResponse(loginUser.getId(), loginUser.getUsername(), accessToken),
			"%s님 환영합니다.".formatted(loginUser.getUsername())
		);

		try (PrintWriter writer = response.getWriter()) {
			writer.write(JsonUtil.toString(success));
			writer.flush();
		}
	}
}
