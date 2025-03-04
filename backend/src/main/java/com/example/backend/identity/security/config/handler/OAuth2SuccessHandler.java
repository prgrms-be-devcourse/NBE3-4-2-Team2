package com.example.backend.identity.security.config.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.jwt.RefreshTokenService;
import com.example.backend.identity.security.user.CustomUser;

import jakarta.servlet.ServletException;
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
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
		throws IOException, ServletException {

		CustomUser loginUser = (CustomUser) authentication.getPrincipal();

		// 새로운 리프레시 토큰 발급 및 쿠키 저장
		refreshTokenService.genRefreshToken(loginUser, response);

		response.sendRedirect("http://localhost:3000/login?oauth2=true");

		// // 새로운 액세스 토큰 발급 및 응답 헤더 추가
		// String accessToken = accessTokenService.genAccessToken(loginUser);
		// response.setHeader("Authorization", "Bearer " + accessToken);
		// response.setStatus(HttpServletResponse.SC_OK);
		// response.setContentType("application/json");
		// response.setCharacterEncoding("UTF-8");



		// RsData<MemberLoginResponse> success = RsData.success(
		// 	new MemberLoginResponse(loginUser.getId(), loginUser.getUsername(), accessToken),
		// 	"%s님 환영합니다.".formatted(loginUser.getUsername())
		// );
		//
		// try (PrintWriter writer = response.getWriter()) {
		// 	writer.write(JsonUtil.toString(success));
		// 	writer.flush();
		// }
	}
}
