package com.example.backend.identity.security.user.service;

import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MemberRepository;
import com.example.backend.identity.security.jwt.AccessTokenService;
import com.example.backend.identity.security.jwt.RefreshTokenService;
import com.example.backend.identity.security.user.CustomUser;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberRepository memberRepository;
	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;

	@Override
	public CustomUser loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberEntity member = memberRepository.findByUsername(username)
								.orElseThrow(()->new UsernameNotFoundException("username not found : " + username));
		return new CustomUser(member, null);
	}

	public CustomUser loadUserById(long id) throws UsernameNotFoundException {
		MemberEntity member = memberRepository.findById(id)
			.orElseThrow(() -> new AuthenticationException("id에 맞는 멤버가 존재하지 않습니다.") {
			});
		return new CustomUser(member, null);
	}

	public CustomUser getUserByAccessToken(String accessToken) {
		try {
			Map<String, Object> payload = accessTokenService.getAccessTokenPayload(accessToken);
			String username = accessTokenService.getSubject(accessToken);

			long id = ((Number) payload.get("id")).longValue();

			MemberEntity actor = new MemberEntity(id, username);
			return new CustomUser(actor, null);
		} catch (JwtException e) {
			return null;
		}

	}

	public CustomUser getUserByRefreshToken(String refreshToken) {
		try {
			Map<String, Object> payload = refreshTokenService.getRefreshTokenPayload(refreshToken); // jwt가 유효하지 않다면 에러를 던진다.
			String username = refreshTokenService.getSubject(refreshToken);

			long id = ((Number) payload.get("id")).longValue();

			MemberEntity actor = new MemberEntity(id, username);
			return new CustomUser(actor, null);
		} catch (JwtException e) {
			// Jwt 에러는 굳이 예외처리 하지 않음. // todo : 필터 순서를 조정해서 예외로 처리하는게 더 적절하려나
			return null;
		}
	}
}
