package com.example.backend.identity.security.jwt;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.util.JwtUtil;
import com.example.backend.identity.security.user.CustomUser;

import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Service
public class AccessTokenService {

	@Value("${custom.jwt.accessToken.secretKey}")
	private String accessTokenSecretKey;

	@Getter
	@Value("${custom.jwt.accessToken.expirationSeconds}")
	private long accessTokenExpirationSeconds;

	public String genAccessToken(CustomUser customUser) {
		return JwtUtil.generateToken(
			accessTokenSecretKey,
			accessTokenExpirationSeconds,
			Map.of("id", customUser.getId(), "username", customUser.getUsername())
		);
	}

	public String genAccessToken(MemberEntity member) {
		return JwtUtil.generateToken(
			accessTokenSecretKey,
			accessTokenExpirationSeconds,
			Map.of("id", member.getId(), "username", member.getUsername())
		);
	}

	public Map<String, Object> getAccessTokenPayload(String accessToken) throws JwtException {
		return JwtUtil.getPayload(accessToken, accessTokenSecretKey);
	}

}
