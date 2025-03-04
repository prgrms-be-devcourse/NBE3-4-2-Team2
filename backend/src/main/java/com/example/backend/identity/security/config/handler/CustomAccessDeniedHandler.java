package com.example.backend.identity.security.config.handler;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAccessDeniedHandler는 사용자가 권한 없는 리소스에 접근할 때,
 * GlobalExceptionHandler로 예외를 전달해주는 클래스 입니다.
 * <p>
 * 이 클래스는 Spring Security에서 액세스 거부(Exception) 처리 시 사용됩니다. (403에러)
 * </p>
 *
 * @Author k-haechan
 * @Since 25.02.10
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다."); // todo : 양식에 맞게 추후에 변경
	}
}
