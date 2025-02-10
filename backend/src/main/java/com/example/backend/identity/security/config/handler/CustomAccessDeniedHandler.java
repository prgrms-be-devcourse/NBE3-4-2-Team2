package com.example.backend.identity.security.config.handler;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.backend.global.rs.ErrorRs;
import com.example.backend.global.rs.RsData;
import com.example.backend.global.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAccessDeniedHandler는 사용자가 권한 없는 리소스에 접근할 때, 403 Forbidden 상태 코드와
 * 사용자 정의 오류 메시지를 포함한 JSON 응답을 반환하는 핸들러입니다.
 * <p>
 * 이 클래스는 Spring Security에서 액세스 거부(Exception) 처리 시 사용됩니다.
 * </p>
 *
 * @Author k-haechan
 * @Since 25.02.10
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	/**
	 * 권한 없는 접근을 시도할 때 호출됩니다. 요청 URI와 함께 403 상태 코드와
	 * 사용자 정의 오류 메시지를 포함한 JSON 응답을 반환합니다.
	 *
	 * @param request                HttpServletRequest 객체, 요청 정보 제공
	 * @param response               HttpServletResponse 객체, 응답 정보 설정
	 * @param accessDeniedException  권한 거부 예외, 예외 정보 제공
	 * @throws IOException           입출력 예외 발생 시
	 * @throws ServletException      서블릿 관련 예외 발생 시
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
		throws IOException, ServletException {

		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// RsData 생성
		String errorResponse = JsonUtil.toString(
			RsData.error(
				ErrorRs.builder()
					.target(request.getRequestURI())
					.code(403)
					.message("해당 리소스에 권한이 없습니다.")
					.build()
			)
		);
		// JSON 응답 반환
		try (PrintWriter writer = response.getWriter()) {
			writer.write(errorResponse);
			writer.flush();
		}
	}
}
