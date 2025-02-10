package com.example.backend.identity.security.config;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.backend.identity.member.dto.login.MemberLoginRequest;
import com.example.backend.identity.security.config.handler.CustomSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	public CustomUsernamePasswordAuthenticationFilter(
		AuthenticationManager authenticationManager,
		CustomSuccessHandler successHandler) {
		super.setAuthenticationManager(authenticationManager);
		super.setAuthenticationSuccessHandler(successHandler);
		setFilterProcessesUrl("/api-v1/members/login"); //
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		try {
			// request에서 json 파싱하여 loginRequest에 대입
			ObjectMapper objectMapper = new ObjectMapper();
			MemberLoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), MemberLoginRequest.class);

			// loginRequest의 username, password를 UsernamePasswordToken으로 변환
			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

			// AuthenticationManager가 AuthenticationProvider를 통하여 토큰을 이용한 인증 실행(username, password 확인)
			return getAuthenticationManager().authenticate(authToken);

		} catch (IOException e) {
			throw new AuthenticationServiceException("Failed to parse JSON object", e);
		}
	}

}
