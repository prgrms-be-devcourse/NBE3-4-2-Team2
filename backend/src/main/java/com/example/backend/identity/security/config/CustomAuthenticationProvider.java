package com.example.backend.identity.security.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.backend.identity.security.user.CustomUser;
import com.example.backend.identity.security.user.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;


/**
 * @author k-haechan
 * @since 25.02.10
 * */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
	private final CustomUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// UsernamePasswordToken에서 로그인에 필요한 정보 추출
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		// username으로 DB에서 값 조회
		CustomUser userDetails = userDetailsService.loadUserByUsername(username);

		// 비밀번호 인증 확인
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new BadCredentialsException("유효하지 않은 비밀번호입니다.");
		}

		// 인증이 완료되면 UsernamePasswordToken에 DB에서 조회한 유저정보와 권한을 추가하여 반환 (비밀번호는 보안상 null 처리)
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) { // 이 Provider는 UsernamePasswordAuthentication을 담당한다.
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
