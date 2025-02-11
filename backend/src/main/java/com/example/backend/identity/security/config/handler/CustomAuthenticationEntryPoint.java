package com.example.backend.identity.security.config.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CustomAuthenticationEntryPoint는 인증되지 않은 사용자 또는 잘못된 인증 정보를 가진 사용자가
 * 보호된 리소스에 접근할 때, GlobalExceptionHandler를 통해 401에러를 반환하는 클래스 입니다.
 *
 * @Author k-haechan
 * @Since 25.02.10
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final HandlerExceptionResolver exceptionResolver;

	CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
		this.exceptionResolver = exceptionResolver;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {
		// 내부적인 로그를 찍어보려면 이곳에서 하면 됨!

		// AuthenticationException은 401에러로 GlobalExceptionHandler에서 한번에 처리
		exceptionResolver.resolveException(request, response, null, authException);
	}
}
