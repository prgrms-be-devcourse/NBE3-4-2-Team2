package com.example.backend.identity.security.config.handler;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.backend.global.rs.ErrorRs;
import com.example.backend.global.rs.RsData;
import com.example.backend.global.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAuthenticationEntryPoint는 인증되지 않은 사용자 또는 잘못된 인증 정보를 가진 사용자가
 * 보호된 리소스에 접근할 때, 401 Unauthorized 상태 코드와 사용자 정의 오류 메시지를
 * 포함한 JSON 응답을 반환하는 핸들러입니다.
 * <p>
 * 이 클래스는 Spring Security에서 인증 실패 시 사용자에게 적절한 오류 메시지를 제공하는 역할을 합니다.
 * </p>
 *
 * @Author k-haechan
 * @Since 25.02.10
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	/**
	 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출됩니다.
	 * 요청 URI와 함께 401 상태 코드와 사용자 정의 오류 메시지를 포함한 JSON 응답을 반환합니다.
	 *
	 * @param request                HttpServletRequest 객체, 요청 정보 제공
	 * @param response               HttpServletResponse 객체, 응답 정보 설정
	 * @param authException          인증 예외, 인증 실패 정보 제공
	 * @throws IOException           입출력 예외 발생 시
	 * @throws ServletException      서블릿 관련 예외 발생 시
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {

		// 401 Unauthorized 상태 코드 설정
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		// 응답 형식: JSON
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// 오류 메시지 설정
		String errorResponse = JsonUtil.toString(
			RsData.error(
				ErrorRs.builder()
					.target(request.getRequestURI())
					.message("사용자 인증정보가 올바르지 않습니다.")
					.build()
			)
		);

		// JSON 오류 메시지 반환
		try (PrintWriter writer = response.getWriter()) {
			writer.write(errorResponse);
			writer.flush();
		}
	}
}
