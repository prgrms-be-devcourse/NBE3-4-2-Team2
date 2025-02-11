package com.example.backend.identity.security.config.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAccessDeniedHandler는 사용자가 권한 없는 리소스에 접근할 때,
 * GlobalExceptionHandler로 예외를 전달해주는 클래스 입니다.
 * <p>
 * 이 클래스는 Spring Security에서 액세스 거부(Exception) 처리 시 사용됩니다.
 * </p>
 *
 * @Author k-haechan
 * @Since 25.02.10
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private final HandlerExceptionResolver exceptionResolver;

	CustomAccessDeniedHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
		this.exceptionResolver = exceptionResolver;
	}
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
		throws IOException, ServletException {

		// 내부적인 로그를 찍어보려면 이곳에서 하면 됨!

		// AuthenticationException은 401에러로 GlobalExceptionHandler에서 한번에 처리
		exceptionResolver.resolveException(request, response, null, accessDeniedException);
	}
}
