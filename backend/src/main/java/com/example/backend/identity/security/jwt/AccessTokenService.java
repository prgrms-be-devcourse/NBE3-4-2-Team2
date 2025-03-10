package com.example.backend.identity.security.jwt;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.util.JwtUtil;
import com.example.backend.identity.security.user.CustomUser;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
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
			customUser.getUsername(),
			Map.of("id", customUser.getId()),
			accessTokenSecretKey,
			accessTokenExpirationSeconds
		);
	}

	public void genAccessToken(CustomUser loginUser, HttpServletResponse response) {
		String accessToken = genAccessToken(loginUser);
		response.setHeader("Authorization", "Bearer " + accessToken);
	}

	// todo : Deprecated 예정
	public String genAccessToken(MemberEntity member) {
		return JwtUtil.generateToken(
			member.getUsername(),
			Map.of("id", member.getId()),
			accessTokenSecretKey,
			accessTokenExpirationSeconds
		);
	}

	public Map<String, Object> getAccessTokenPayload(String accessToken) throws JwtException {
		return JwtUtil.getPayload(accessToken, accessTokenSecretKey);
	}

	public String getSubject(String accessToken) {
		return JwtUtil.getSubject(accessToken, accessTokenSecretKey);
	}
}
