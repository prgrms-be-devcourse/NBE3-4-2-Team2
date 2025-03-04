package com.example.backend.identity.security.config.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 과정 중 인증 실패 시 실행되는 핸들러
 * */
@Component
@Slf4j
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {
		// response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.!!"); // todo : 양식에 맞게 추후에 변경
		log.error("OAuth2 인증 실패 : {}", exception.getMessage());
		response.sendRedirect("http://localhost:3000/login?error=true");
	}
}
